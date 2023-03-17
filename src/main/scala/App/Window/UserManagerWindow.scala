package App

import App.Window.AddState
import Mode.{Event, ExEvent}
import Mode.Event.NAE
import Mode.ModeType.AppType
import zio.Console.printLine
import zio.{Task, ZIO}

import java.io.IOException
import scala.collection.mutable.Map
/*
case class UserManagerWindow(val mode: AppMode) extends Window {
  val name = "Add Book"
  var state: AddState = AddState.Enter("name", false)

  val bookMap: Map[String, String] = Map("name" -> "", "author" -> "", "genre" -> "")

  def print(): ZIO[Any, IOException, Unit] = for {
    _ <- printLine(s"   From here it is possible to administer the user database")
    _ <- printLine("     > Add User [A]    > Remove User [R]")
  } yield ()

  def printSelector(field: String): String = if (isSelectedField(field)) "  ->  " else "    "

  def isSelectedField(field: String): Boolean = state match {
    case AddState.Enter(tag, _) => tag == field
    case _ => false
  }

  def printKeymap(): ZIO[Any, IOException, Unit] = printLine("Select fields with arrow. [A] to Add book")

  def keymap(tag: String): Event[AppType] = tag match {
    case "u" => AddEvent.SelectField(true)
    case "d" => AddEvent.SelectField(false)
    case "A" => AddEvent.AddBook()
    case "R" => AddEvent.RemoveBook()
    case _ => AddEvent.SetBook(tag)
  }

  trait AddEvent extends ExEvent[AppType]

  object AddEvent {
    case class SetBook(tag: String) extends AddEvent {
      def execute(): Task[Event[AppType]] = {
        state match {
          case AddState.Enter(field, _) => bookMap(field) = tag
        }
        NAE
      }
    }


    case class AddBook() extends AddEvent {
      def execute(): Task[Event[AppType]] = {
        mode.bookdb.add(Book.make(bookMap))
        state = state match {
          case AddState.Enter(field, _) => AddState.Enter(field, true)
        }
        bookMap("name") = ""
        bookMap("author") = ""
        bookMap("genre") = ""
        state = AddState.Enter("author", false)
        NAE
      }
    }

    case class RemoveBook() extends AddEvent {
      def execute(): Task[Event[AppType]] = {
        mode.bookdb.removeAll(bookMap("name"))
        state = state match {
          case AddState.Enter(field, _) => AddState.Enter(field, true)
        }
        bookMap("name") = ""
        bookMap("author") = ""
        bookMap("genre") = ""
        state = AddState.Enter("author", false)
        NAE
      }
    }

    case class SelectField(up: Boolean) extends AddEvent {
      def execute(): Task[Event[AppType]] = if (up) {
        state = state match {
          case AddState.Enter("genre", v) => AddState.Enter("author", v)
          case AddState.Enter(_, v) => AddState.Enter("name", v)
        }
        NAE
      } else {
        state = state match {
          case AddState.Enter("name", v) => AddState.Enter("author", v)
          case AddState.Enter(_, v) => AddState.Enter("genre", v)
        }
        NAE
      }
    }


  }

}

*/