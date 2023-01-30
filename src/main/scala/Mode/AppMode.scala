package Mode

import App._
import Controller.Event
import Controller.Event._
import DB._
import Mode.ModeType.{AppType, ExitType}
import Recommendation.Recommender
import zio.Console.printLine
import zio.{IO, Ref, ZIO}

import java.io.IOException
import scala.collection.mutable.ListBuffer

case class AppMode(val user: Profile, override val eventQueue: zio.Queue[Event[AppType]], override val mustReprint: zio.Ref[Boolean],
                   override val continue: Ref[Boolean]) extends Mode[AppType] {
  var mainWindow: Int = 0
  var updated: Boolean = false

  val bookdb: BookDB = BookDB("a")

  var windows: ListBuffer[Window] = ListBuffer(SearchWindow(this))
  val recommender: Recommender = new Recommender()
  val recWindow: RecWindow = new RecWindow(recommender)


  //print routine...
  def print(): ZIO[Any, IOException, Unit] = for {
    _ <- header()
    _ <- windows(mainWindow).print()
    _ <- recWindow.print()
    _ <- tabs(windows)
    _ <- windows(mainWindow).printKeymap()
    _ <- standardKeymap()
  } yield ()


  //submethods for printing parts
  def header(): ZIO[Any, IOException, Unit] =
    printLine(s"Welcome to the Collectionist, ${user.name}!") //put type of user???

  def tabs(windows: ListBuffer[Window]): ZIO[Any, IOException, Unit] = if(windows.nonEmpty) {for {
    _ <- if(windows.head.updated) zio.Console.print("| " + "  " + windows.head.name + " !! |")
    else zio.Console.print("| " + "  " + windows.head.name + "    |")
    _ <- tabs(windows.tail)
  } yield ()} else printLine("")


  def standardKeymap(): ZIO[Any, IOException, Unit] =
    printLine("Search [S]      Add book [A]      Remove Book [R]      Profile configuration [C]      Exit [E]")




  // methods for events
  def command(tag: String): Event[AppType] = {
    tag match {
      case Int(n) => AppEvent.GotoProcess(n)
      case "S" => AppEvent.Open(new SearchWindow(this))
      case "A" => AppEvent.Open(new AddWindow(this))
      //case "R" => AppEvent.Open(new RemoveWindow(this))
      //case "C" => AppEvent.Open(new ConfigurationWindow(this))
      case "Q" => AppEvent.QuitWindow()
      case "E" => ToExit()
      case _ => {
        windows(mainWindow).keymap(tag)
      }
    }
  }

  object Int {
    def unapply(s: String): Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }

  //actualize debe realmente poner en cola el evento y response inicializar todos los eventos que queden por inicializar y devolver el primero en la cola de devoler
  //def actualize(newEvent: ExEvent[AppType]): Unit = newEvent.execute()
  /*
    def initialize(): ZIO[Any, Throwable, Unit] = if (waitingProcess.isEmpty){
      ZIO.succeed({})
    } else {
      for {
        _ <- waitingProcess.remove(0).fiber.forkDaemon
      } yield()
    }

    //def response(): ZIO[Any, Throwable, Event[AppType]] = ZIO.succeed(waitingEvents(0))

    def response(): ZIO[Any, Throwable, Event[AppType]] = for {
      _ <- initialize
      fstResponse <- ZIO.raceAll(onGoingProcess.map(_ => getFiber(_))) // ESTO COMO HACERLO MEJOR
    } yield (fstResponse)

    def clean(): Unit = for(process <- onGoingProcess){
      if(process.finished)
    }


    def getFiber(process: Process): Fiber[Throwable, Event[AppType]] = process.fiber
  */
  //lo que tendría que hacer response es:
  /*
  inicia procesos sin iniciar puestos en cola por actualizar:
    los inicia, los pone en ongoing y los quita de waiting
  espera a que alguno de los procesos ongoing devuelva algo y lo devuelve
   */







  object AppEvent {
    final case class GotoProcess(n: Int) extends AppEvent {
      def execute(): Event[AppType] = {
        mainWindow = n
        NAE
      }
    }

    final case class Open(window: Window) extends AppEvent {
      def execute(): Event[AppType]= {
        windows.append(window)
        mainWindow = windows.length-1
        NAE
      }
      /*
      if(process: Configuration) {
        ... // no permitirlo si ya existe
      } else {
        toStartProcess +: process
        ongoingProcess +: process
        mainProcess = process

      }*/
    }

    final case class QuitWindow() extends AppEvent {
      def execute(): Event[AppType] = {
        windows.drop(mainWindow)
        NAE
      }
    }






  }

  case class ToExit() extends ChangeMode[AppType] {
    type nextType = ExitType
    val nextMode = ExitMode()
  }



}

object AppMode{
  def apply(user: Profile): IO[Throwable, AppMode] = {
    for {
      ref <- Ref.make(true)
      continue <- Ref.make(true)
      queue <- zio.Queue.unbounded[Event[AppType]]
    } yield AppMode(user, queue, ref, continue)
  }
}
