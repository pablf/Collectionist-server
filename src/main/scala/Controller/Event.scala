package Controller

import Mode.{Mode, ModeType}
import _root_.Mode.ModeType.{AppType, LoginType}
import zio.{IO, ZIO}

trait Event[+SomeType <: ModeType]{

}

trait ExEvent[+SomeType <: ModeType] extends Event[SomeType] {
  def execute(): Event[SomeType]
  //val mode
}







object Event {
  abstract class ChangeMode[+SomeType <: ModeType]() extends Event[SomeType]{
    type nextType <: ModeType
    val nextMode: IO[Throwable, Mode[nextType]]
  }

  trait AppEvent extends ExEvent[AppType]
  val NAE = NullEvent[AppType]()//Null App Event: NAE

  trait LoginEvent extends ExEvent[LoginType]
  val NLE = NullEvent[LoginType]() //Null Login Event: NLE
  final case class TerminateEvent[+SomeType <: ModeType]() extends Event[SomeType] //Event to end app

  final case class NullEvent[+SomeType <: ModeType]() extends Event[SomeType] // Event that does nothing
}
