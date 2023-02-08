package App

import Controller.Event.{ChangeMode, NAE, NullEvent}
import Controller.{Event, ExEvent}
import Mode.{AppMode, LoginMode, Mode}
import _root_.Mode.ModeType.{AppType, LoginType}
import State.ConfigurationState
import zio.Console.printLine
import zio.{IO, Task, ZIO}

import java.awt.desktop.AppEvent
import java.io.IOException

case class ConfigurationWindow(val mode: AppMode) extends Window {
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
    case "P" => ConfigurationEvent.ChangePassword()
    case "D" => ConfigurationEvent.DeleteUser()
    case _ => state match {
      case ConfigurationState.Menu() => NullEvent[AppType]()
      case ConfigurationState.AskCurrentPassword() => ConfigurationEvent.CheckOldPassword(tag)
      case ConfigurationState.AskNewPassword() => ConfigurationEvent.SetNewPassword(tag)
    }
  }


  trait ConfigurationEvent extends ExEvent[AppType]

  object ConfigurationEvent {
    final case class ChangePassword() extends ConfigurationEvent {
      def execute(): Task[Event[AppType]] = {
        state = ConfigurationState.AskCurrentPassword()
        NAE
      }
    }

    final case class CheckOldPassword(tag: String) extends ConfigurationEvent {
      def execute(): Task[Event[AppType]] = {
        if(mode.user.checkPass(tag)) state = ConfigurationState.AskNewPassword()
        NAE
      }
    }

    final case class SetNewPassword(tag: String) extends ConfigurationEvent {
      def execute(): Task[Event[AppType]] = {
        mode.user.changePass(tag)
        state = ConfigurationState.Menu()
        NAE
      }
    }

    final case class DeleteUser() extends ConfigurationEvent {
      def execute(): Task[Event[AppType]] = {
        mode.user.delete()
        state = ConfigurationState.Menu()
        ZIO.succeed(new ChangeMode[AppType] {
          type nextType = LoginType
          val nextMode: IO[Throwable, Mode[LoginType]] = LoginMode()
        })
      }

    }

  }


}