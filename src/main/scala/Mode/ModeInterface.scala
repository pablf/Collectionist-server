package Mode

import DB.{BookDB, RatingDB, UserDB}
import Mode.Event.TerminateEvent
import Recommendation.Recommender
import Validator.UserValidator
import zio.{&, IO, Queue, Ref, ZIO}

import java.io.IOException

/*
Main.loop interchanges Modes
There are: AppMode, LoginMode, ExitMode
This class contains generic methods to print, get input and actualize the state of the mode
 */

trait ModeInterface[Type <: ModeType] {
  // state of the mode
  //val state: Ref[State[Type]]
  val eventQueue: Queue[Event[Type]]
  val mustReprint: Ref[Boolean]
  val continue: Ref[Boolean]
  val lastEvent: Ref[Event[Type]]

  def reprint(): ZIO[Any, IOException, Unit]

  def catchEvent(): IO[IOException, Unit]

  def actualize(): ZIO[UserValidator &
    RatingDB &
    UserDB &
    BookDB, Throwable, Boolean]
}
