package App

import App.Window.{ConfigurationState, Window}
import Mode.Event.{ChangeMode, NAE, NullEvent}
import DB.Book
import Login.LoginMode
import Mode.{Event, ExEvent, Mode}
import _root_.Mode.ModeType.{AppType, LoginType}
import Validator.UserValidator
import zio.Console.printLine
import zio.{IO, Task, ZIO}

import java.awt.desktop.AppEvent
import java.io.IOException

case class ConfigurationWindow(val mode: AppMode, val validator: UserValidator) extends Window {
  val name = "Configuration"
  var state: ConfigurationState = ConfigurationState.Menu()

  var currentBook: Option[Book] = None
  var currentBooks: List[Book] = List()

  def print(): ZIO[Any, IOException, Unit] = state match {
    case ConfigurationState.Menu() => printLine("You may change your password [P] or delete your profile [D]")
    case ConfigurationState.AskCurrentPassword() => printLine("Enter your current password first")
    case ConfigurationState.AskNewPassword() => printLine("Now you can enter your new password")
  }

  def printKeymap(): ZIO[Any, IOException, Unit] = printLine("You may change your password [P] or delete your profile [D]")

  def printBooks(): ZIO[Any, IOException, Unit] = for {
    _ <- ZIO.foreach(currentBooks)(book => printLine(s" ->    ${book.name}, by ${book.author}") *> printLine(s" Genre: ${book.genre}"))
  } yield ()



  def keymap(tag: String): Event[AppType] = tag match {
    case "P" => ConfigurationEvent.ChangeState(true, ConfigurationState.AskCurrentPassword())
    case "D" => ConfigurationEvent.DeleteUser()
    case _ => state match {
      case ConfigurationState.Menu() => NullEvent[AppType]()
      case ConfigurationState.AskCurrentPassword() => ConfigurationEvent.CheckOldPassword(tag)
      case ConfigurationState.AskNewPassword() => ConfigurationEvent.SetNewPassword(tag)
    }
  }


  trait ConfigurationEvent extends ExEvent[AppType]

  object ConfigurationEvent {
    final case class ChangeState(mustChange: Boolean, nextState: ConfigurationState) extends ConfigurationEvent {
      def execute(): Task[Event[AppType]] = {
        state = nextState
        NAE
      }


      // if(mustChange) ZIO.succeed(state = nextState) *> NAE else NAE
    }


    final case class CheckOldPassword(tag: String) extends ConfigurationEvent {
      def execute(): Task[Event[AppType]] = for {
        isCorrect <- validator.checkSecond(tag)
      } yield ChangeState(isCorrect, ConfigurationState.AskNewPassword())
    }

    final case class SetNewPassword(tag: String) extends ConfigurationEvent {
      def execute(): Task[Event[AppType]] = for {
        successful <- validator.addSecond(tag)
      } yield ChangeState(successful, ConfigurationState.Menu())
    }

    final case class DeleteUser() extends ConfigurationEvent {
      def execute(): Task[Event[AppType]] = validator.delete(mode.user.name) *>
        ZIO.succeed(new ChangeMode[AppType] {
          type nextType = LoginType
          val nextMode: IO[Throwable, Mode[LoginType]] = LoginMode()})
    }

  }


}

object ConfigurationWindow {
  def apply(mode: AppMode): IO[Throwable, ConfigurationWindow] = for {
    validator <- UserValidator("users")
  } yield ConfigurationWindow(mode, validator)
}