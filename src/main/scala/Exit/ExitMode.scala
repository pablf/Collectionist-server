package Exit

import Mode.Event.{ChangeMode, TerminateEvent}
import Login.LoginMode
import Mode.ModeType.{ExitType, LoginType}
import Mode.{Event, Mode}
import zio.Console.printLine
import zio.{IO, Ref, UIO, ZIO}

import java.io.IOException

case class ExitMode(override val eventQueue: zio.Queue[Event[ExitType]],
                    override val mustReprint: zio.Ref[Boolean],
                    override val continue: Ref[Boolean],
                    override val lastEvent: Ref[Event[ExitType]]
                   ) extends Mode[ExitType] {

  override def print(): ZIO[Any, IOException, Unit] = printLine("Do you really want to go [E] or login again [N]?")


  //command is wrapped in ZIO to deal with asynchronous access in other modes, keymap deals with cases that do not need ZIO,
  // command must TODO  ??? hacerlo en Mode
  override def command(tag: String): UIO[Event[ExitType]] = ZIO.succeed(keymap(tag))

  def keymap(tag: String) = tag match {
    case "E" => TerminateEvent[ExitType]()
    case "N" => ToLogin()
    case _ => TerminateEvent[ExitType]()
  }

  case class ToLogin() extends ChangeMode[ExitType]{
    type nextType = LoginType
    val nextMode = LoginMode()
  }


}


object ExitMode {
  def apply(): IO[Throwable, ExitMode] = {
    for {
      ref <- Ref.make(true)
      queue <- zio.Queue.unbounded[Event[ExitType]]
      continue <- Ref.make(true)
      lastEvent <- Ref.make[Event[ExitType]](new TerminateEvent[ExitType])
    } yield ExitMode(queue, ref, continue, lastEvent)
  }
}
