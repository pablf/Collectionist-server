package Login

import App.{AppMode, Profile}
import Mode.Event.{ChangeMode, LoginEvent, NLE, TerminateEvent}
import Mode.{Event, Mode}
import Validator.UserValidator
import _root_.Mode.ModeType.{AppType, LoginType}
import zio.Console._
import zio._

import java.io.IOException

final case class LoginMode(override val eventQueue: zio.Queue[Event[LoginType]],
                     override val mustReprint: zio.Ref[Boolean],
                     override val continue: Ref[Boolean],
                     override val lastEvent: Ref[Event[LoginType]],
                     validator: UserValidator
                    ) extends Mode[LoginType] {
  var state: LoginState = LoginState.LoginUser()
  var user: String = ""
  var password: String = ""


  def print(): ZIO[Any, IOException, Unit] = state match {
    case _: LoginState.LoginUser => printLine("Who are you?  ([N] for new user)")
    case _: LoginState.LoginPassword => printLine("Enter password")
    case _: LoginState.Wrong => printLine("I do not know you")
    case _: LoginState.CreateUser => printLine("Welcome, new user! Tell us you name")
    case _: LoginState.CreatePassword => printLine("Choose a password")
    case _ => printLine("Something went off. This place feels strangely eerie...")
  }


  def command(tag: String): UIO[Event[LoginType]] = ZIO.succeed(keymap(tag))

  def keymap(tag: String): Event[LoginType] = {
    tag match {
      case "N" => LoginEvent.ChangeState(true, LoginState.CreateUser())
      case _ => state match {
        case _: LoginState.LoginUser => LoginEvent.LoginUser(tag)
        case _: LoginState.LoginPassword => LoginEvent.LoginPassword(tag)
        case _: LoginState.Wrong => LoginEvent.ChangeState(true, LoginState.LoginUser())
        case _: LoginState.CreateUser => LoginEvent.CreateUser(tag)
        case _: LoginState.CreatePassword => LoginEvent.CreateUser(tag)
      }
    }
  }



  object LoginEvent {
    final case class LoginUser(tag: String) extends LoginEvent {

      def execute(): Task[Event[LoginType]] = for {
        isValid <- validator.checkFirst(tag)
      } yield ChangeState(isValid, LoginState.LoginPassword())

    }

    //TODO añadir id a user
    final case class LoginPassword(tag: String) extends LoginEvent {

      def execute(): Task[Event[LoginType]] = for {
        isCorrect <- validator.checkSecond(tag)
        nextEvent <- for {
          id <- validator.id.get
          profile <- Profile(user, id)
        } yield ToApp(profile)
      } yield nextEvent
    }

    final case class CreateUser(tag: String) extends LoginEvent {

      def execute(): Task[Event[LoginType]] = for {
        did <- validator.addFirst(tag)
      } yield ChangeState(did, LoginState.CreatePassword())
    }

    final case class CreatePassword(tag: String) extends LoginEvent {

      def execute(): Task[Event[LoginType]] = for {
        did <- validator.addSecond(tag)
      } yield ChangeState(did, LoginState.LoginUser())
    }

    final case class ChangeState(mustChange: Boolean, nextState: LoginState) extends LoginEvent {
      def execute(): Task[Event[LoginType]] = {
        state = nextState
        NLE
      }
    }



  }

    case class ToApp(val profile: Profile) extends ChangeMode[LoginType]{
      type nextType = AppType
      val nextMode = AppMode(profile)
    }




}

object LoginMode {
  def apply(): ZIO[UserValidator, Throwable, LoginMode] = {
    for {
      queue <- zio.Queue.unbounded[Event[LoginType]]
      ref <- Ref.make(true)
      continue <- Ref.make(true)
      lastEvent <- Ref.make[Event[LoginType]](new TerminateEvent[LoginType])
      validator <- ZIO.service[UserValidator]
    } yield LoginMode(queue, ref, continue, lastEvent, validator)
  }
}
