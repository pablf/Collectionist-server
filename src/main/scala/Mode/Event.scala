package Mode

import DB.{BookDB, RatingDB, UserDB}
import Recommendation.Recommender
import Validator.UserValidator
import _root_.Mode.ModeType.{AppType, LoginType}
import zio.{&, IO, Task, ZIO}

trait Event[+SomeType <: ModeType]{

}

trait ExEvent[+SomeType <: ModeType] extends Event[SomeType] {
  def execute(): ZIO[UserValidator &
    Recommender &
    RatingDB &
    UserDB &
    BookDB, Throwable, Event[SomeType]]
  //val mode
}







object Event {
  abstract class ChangeMode[+SomeType <: ModeType]() extends Event[SomeType]{
    type nextType <: ModeType
    val nextMode: ZIO[UserValidator &
      Recommender &
      RatingDB &
      UserDB &
      BookDB, Throwable, Mode[nextType]]
  }

  trait AppEvent extends ExEvent[AppType]
  val NAE = ZIO.succeed(NullEvent[AppType]())//Null App Event: NAE

  trait LoginEvent extends ExEvent[LoginType]
  val NLE = ZIO.succeed(NullEvent[LoginType]()) //Null Login Event: NLE
  final case class TerminateEvent[+SomeType <: ModeType]() extends Event[SomeType] //Event to end app

  final case class NullEvent[+SomeType <: ModeType]() extends Event[SomeType] // Event that does nothing

  trait Load[+SomeType <: ModeType] extends ExEvent[SomeType]
}
