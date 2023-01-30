package Mode

import Controller.Event
import Controller.Event.{ChangeMode, TerminateEvent}
import Mode.ModeType.{ExitType, LoginType}
import zio.Console.printLine
import zio.{IO, Ref, ZIO}

import java.io.IOException

case class ExitMode(override val eventQueue: zio.Queue[Event[ExitType]], override val mustReprint: zio.Ref[Boolean], override val continue: Ref[Boolean]) extends Mode[ExitType] {

  def print(): ZIO[Any, IOException, Unit] = printLine("Do you really want to go [E] or login again [N]?")


  def command(tag: String): Event[ExitType] = tag match {
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
    } yield ExitMode(queue, ref, continue)
  }
}
