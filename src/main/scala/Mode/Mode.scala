package Mode

/*
Main.loop interchanges Modes
There are: AppMode, LoginMode, ExitMode
This class contains generic methods to print, get input and actualize the state of the mode
 */

import Controller.Event.{ChangeMode, NullEvent, TerminateEvent}
import Controller.{Event, ExEvent}
import org.fusesource.jansi.Ansi.ansi
import zio.Console.{printLine, readLine}
import zio.{ZIO, _}

import java.io.IOException

//debería poner Mode[T] y que luego T pueda ser una clase que extienda a Mode????
trait Mode[Type <: ModeType] {

  // OUTPUT
  //var mustReprint: Boolean = true
  def reprint(): ZIO[Any, IOException, Unit] = for {
    _ <- ZIO.ifZIO(mustReprint.get)(onTrue = clean() *> print(), onFalse = ZIO.unit)
    _ <- mustReprint.set(false)
  }yield()

  val CleanCode = "\u001b[2J\u001b[;H"
  def clean(): ZIO[Any, IOException, Unit] = printLine(ansi().eraseScreen().cursor(1, 1))

  def print(): ZIO[Any, IOException, Unit]

  // INPUT: wait for readLine and offer corresponding Event with command to EventQueue
  def command(tag: String): Event[Type]

  def catchEvent(): IO[IOException, Unit] = ZIO.ifZIO(continue.get)(
    onTrue = readLine.map(command).flatMap(tag => eventQueue.offer(tag) *> ZIO.unit),
    onFalse = ZIO.unit) /// se puede evitar???


  // Take event from queue, execute it and get an event in return
  def actualize(): IO[Throwable, Boolean] = for {
    event <- eventQueue.take
    //actualize state: mustReprint and shouldContinue
    _ <- mustReprint.set(true)

    shouldContinue <- event match {
      case event: ChangeMode[Type] => mustReprint.set(false) *> {
        lastEvent = event
        ZIO.succeed(false)
      }
      case event: TerminateEvent[Type] => mustReprint.set(false) *> {
        println("Estoy acabando")
        lastEvent = event
        ZIO.succeed(false)
      }
      case _: NullEvent[Type] => ZIO.succeed(true)
      case event: ExEvent[Type] => for {
        ev <- event.execute()
          .retry(Schedule.recurs(5))//.catchSome {//_: NextModeLoadingError}
        _ <- eventQueue.offer(ev)
        continue <- ZIO.succeed(true)
      } yield (continue)
      case _ => ZIO.succeed(false)
    }
    _ <- continue.set(shouldContinue)

  } yield(shouldContinue)






  // state of the mode
  val eventQueue: Queue[Event[Type]]
  val mustReprint: Ref[Boolean]
  val continue: Ref[Boolean]

  var lastEvent: Event[Type] = new TerminateEvent[Type]
}