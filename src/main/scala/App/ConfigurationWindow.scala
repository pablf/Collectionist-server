package App

import Controller.Event.{ChangeMode, NullEvent}
import Controller.{Event, ExEvent}
import Mode.{AppMode, LoginMode, Mode}
import _root_.Mode.ModeType.AppType
import State.ConfigurationState
import zio.Console.printLine
import zio.{IO, ZIO}

import java.awt.desktop.AppEvent
import java.io.IOException

case class ConfigurationWindow(val mode: AppMode) extends Window {
  val name = "Configuration"
  var state: ConfigurationState = ConfigurationState.Menu()

  var currentBook: Option[Book] = None
  var currentBooks: List[Book] = List()

  def print(): ZIO[Any, IOException, Unit] = state match {
    case ConfigurationState.Menu() => printLine("Enter a book name to Configuration.")
    case ConfigurationState.NotFound() => printLine("I am sorry. We could not find your book. Configuration another one!")
    case ConfigurationState.Found() => printLine("Congratulations! Here are the books:") *> printBooks()
  }

  def printKeymap(): ZIO[Any, IOException, Unit] = state match {
    case ConfigurationState.Menu() => printLine("You may change your password [P] or delete your profile [D]")
    case ConfigurationState.AskCurrentPassword() => printLine("Enter your current password first")
    case ConfigurationState.AskNewPassword() => printLine("Now you can enter your new password")
  }

  def printBooks(): ZIO[Any, IOException, Unit] = for {
    _ <- ZIO.foreach(currentBooks)(book =e> printLine(s" ->    ${book.name}, by ${book.author}") *> printLine(s" Genre: ${book.genre}"))
  } yield ()



  def keymap(tag: String): Event[AppType] = tag match {
    case "P" => ConfigurationEvent.ChangePassword
    case "D" => ConfigurationEvent.DeleteUser
    case _ => state match {
      case
      case ConfigurationState.Menu() => NAE
      case ConfigurationState.AskCurrentPassword() => ConfigurationEvent.CheckOldPassword(tag)
      case ConfigurationState.AskNewPassword() => ConfigurationEvent.SetNewPassword(tag)
    }
  }


  trait ConfigurationEvent extends ExEvent[AppType]
  val NAE = NullEvent[AppType]()

  object ConfigurationEvent {
    final case class ChangePassword(tag: String) extends ConfigurationEvent {
      def execute(): Event[AppType] = {
        val books = mode.bookdb.Configuration(tag)
        if (books.nonEmpty) {
          currentBook = Option(books.head)
          currentBooks = books
          updated = true
          state = ConfigurationState.Found()
          NAE
        } else NotFoundBook(tag)
      }
    }

    final case class CheckOldPassword(tag: String) extends ConfigurationEvent {
      def execute(): Event[AppType] = {
        if(mode.user.checkPass(tag)) state = ConfigurationState.AskNewPassword()
        NAE
      }
    }

    final case class SetNewPassword(tag: String) extends ConfigurationEvent {
      def execute(): Event[AppType] = {
        mode.user.changePass(tag)
        state = ConfigurationState.Menu()
        NAE
      }
    }

    final case class DeleteUser(tag: String) extends ConfigurationEvent {
      def execute(): Event[AppType] = {
        mode.user.delete()
        state = ConfigurationState.Menu()
        new ChangeMode[AppType] {
          type nextType = AppType
          val nextMode: IO[Throwable, Mode[AppType]] = ZIO.succeed(LoginMode())
        }
      }

    }

  }


}
*/