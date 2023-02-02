package Mode

import App.Profile
import Controller.Event
import Controller.Event.{ChangeMode, LoginEvent, NLE}
import DB.UserDB
import Mode.ModeType.{AppType, LoginType}
import State.LoginState
import zio.Console._
import zio._

import java.io.IOException

case class LoginMode(override val eventQueue: zio.Queue[Event[LoginType]], override val mustReprint: zio.Ref[Boolean],
                     override val continue: Ref[Boolean]) extends Mode[LoginType] {
  var state: LoginState = LoginState.GetUser()
  var user: String = ""
  var password: String = ""
  val users = UserDB("users")

  def print(): ZIO[Any, IOException, Unit] = state match {
    case _: LoginState.GetUser => printLine("Who are you?  ([N] for new user)")
    case _: LoginState.GetPassword => printLine("Enter password")
    case _: LoginState.Wrong => printLine("I do not know you")
    case _: LoginState.CreateUser => printLine("Welcome, new user! Tell us you name")
    case _: LoginState.CreatePassword => printLine("Choose a password")
    case _ => printLine("Something went off. This place feels strangely eerie...")
  }


  def command(tag: String): Event[LoginType] = {
    tag match {
      case "N" => LoginEvent.NewUser()
      case _ => state match {
        case _: LoginState.GetUser => LoginEvent.User(tag)
        case _: LoginState.GetPassword => LoginEvent.Password(tag)
        case _: LoginState.Wrong => LoginEvent.NewTry()
        case _: LoginState.CreateUser => LoginEvent.CreateUser(tag)
        case _: LoginState.CreatePassword => LoginEvent.CreateUser(tag)
      }
    }
  }



  def checkUser(): Boolean = users.search(user).nonEmpty
  def checkPassword(): Boolean = users.search(user).head.password == password



  object LoginEvent {
    final case class User(tag: String) extends LoginEvent {
      def execute(): Event[LoginType] = {

        user = tag
        state match {
          case _: LoginState.GetUser => if(checkUser()) state = LoginState.GetPassword()
          case _ => if(!checkUser()) state = LoginState.CreatePassword()
        }
        NLE
      }
    }

    final case class Password(tag: String) extends LoginEvent {
      def execute(): Event[LoginType] = {
        password = tag
        if (checkPassword()) {
          ToApp()
        } else NLE
      }
    }

    final case class NewTry() extends LoginEvent {
      def execute(): Event[LoginType] = {
        state = LoginState.GetUser()
        NLE
      }
    }

    final case class NewUser() extends LoginEvent {
      def execute(): Event[LoginType] = {
        state = LoginState.CreateUser()
        NLE
      }
    }

    final case class CreateUser(tag: String) extends LoginEvent {
      def execute(): Event[LoginType] = {
        if (state == LoginState.CreateUser()) {
          user = tag
          state = LoginState.CreatePassword()
        }
        else {
          password = tag
          users.add(DB.User(user, password))

          state = LoginState.GetUser()
        }

        NLE
      }
    }
  }

    case class ToApp() extends ChangeMode[LoginType]{
      type nextType = AppType
      val nextMode = AppMode(new Profile(user))
    }




}

object LoginMode {
  def apply(): IO[Throwable, LoginMode] = {
    for {
      ref <- Ref.make(true)
      continue <- Ref.make(true)
      queue <- zio.Queue.unbounded[Event[LoginType]]
    } yield LoginMode(queue, ref, continue)
  }
}
