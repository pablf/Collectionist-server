package App


import Controller.Event.{NAE, NullEvent}
import Controller.{Event, ExEvent}
import Mode.AppMode
import Mode.ModeType.AppType
import State.AddState
import zio.Console.printLine
import zio.{Task, ZIO}

import scala.collection.mutable.Map
import java.io.IOException

case class AddWindow(val mode: AppMode) extends Window {
  val name = "Add Book"
  var state: AddState = AddState.Enter("name", false)
  val fields = List("name", "author", "genre")
  val bookMap: Map[String, String] = Map("name" -> "", "author" -> "", "genre" -> "")



  def print(): ZIO[Any, IOException, Unit] = for {
    _ <- state match {
      case AddState.Enter(_, true) => printLine(s"   Your book ${bookMap("name")} was added correctly. Add a new book! Enter the parameters:")
      case AddState.Enter(_, false) => printLine("   Add a new book! Enter the parameters:")
    }
    _ <- ZIO.foreach(fields)(field => printLine(s"${printSelector(field)} ${field.capitalize}: ${bookMap(field)}"))
    _ <- printLine("     > Add Book [A]    > Remove Book [R]")
  } yield ()

  def printSelector(field: String): String = if(isSelectedField(field)) "  ->  " else "    "

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
        mode.bookdb.removeAll(Book.make(bookMap))
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
      def execute(): Task[Event[AppType]] = if(up){
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
