package App.Window

import App.AppMode
import DB.{Book, BookDB}
import Mode.Event.NAE
import Mode.{Event, ExEvent}
import Mode.ModeType.AppType
import zio.Console.printLine
import zio.{RIO, Ref, Task, UIO, ZIO}

import scala.collection.mutable.Map
import java.io.IOException

case class AddWindow(override val updated: Ref[Boolean],
                     override val state: Ref[AddState],
                     mode: AppMode) extends Window[AddState] {
  val name = "Add Book"
  val fields = List("name", "author", "genre")
  val bookMap: Map[String, String] = Map("name" -> "", "author" -> "", "genre" -> "")


  def print(): ZIO[Any, IOException, Unit] = for {
    stateValue <- state.get
    _ <- stateValue match {
      case AddState.Enter(_, true) => printLine(s"   Your book ${bookMap("name")} was added correctly. Add a new book! Enter the parameters:")
      case AddState.Enter(_, false) => printLine("   Add a new book! Enter the parameters:")
    }
    _ <- ZIO.foreach(fields)(field => printLine(s"${printSelector(field)} ${field.capitalize}: ${bookMap(field)}"))
    _ <- printLine("     > Add Book [A]    > Remove Book [R]")
  } yield ()

  def printSelector(field: String): UIO[String] = isSelectedField(field).flatMap{
    b => ZIO.succeed(if (b) "  ->  " else "    ")
  }

  def isSelectedField(field: String): UIO[Boolean] = for {
    stateValue <- state.get
  } yield stateValue match {
    case AddState.Enter(tag, _) => tag == field
    case _ => false
  }


  def printKeymap(): ZIO[Any, IOException, Unit] = printLine("Select fields with arrow. [A] to Add book")

  def keymap(tag: String): UIO[Event[AppType]] = ZIO.succeed(keymapSync(tag))

  def keymapSync(tag: String): Event[AppType]= tag match {
    case "u" => AddEvent.SelectField(true)
    case "d" => AddEvent.SelectField(false)
    case "A" => AddEvent.AddBook()
    case "R" => AddEvent.RemoveBook()
    case _ => AddEvent.SetBook(tag)
  }

  trait AddEvent extends ExEvent[AppType]

  object AddEvent {
    case class SetBook(tag: String) extends AddEvent {
      def execute(): Task[Event[AppType]] = for {
        stateValue <- state.get
        _ <- stateValue match {
          case AddState.Enter(field, _) => ZIO.succeed(bookMap(field) = tag)
        }
        ev <- NAE
      } yield ev

    }


    case class AddBook() extends AddEvent {
      def execute(): RIO[BookDB, Event[AppType]] = for {
        bookdb <- ZIO.service[BookDB]
        _ <- bookdb.add(Book.make(bookMap))
        _ <- ZIO.succeed(bookMap("name") = "")
        _ <- ZIO.succeed(bookMap ("author") = "")
        _ <- ZIO.succeed(bookMap ("genre") = "")
        _ <- state.set(AddState.Enter("author", false))
        ev <- NAE
      } yield ev
    }

    case class RemoveBook() extends AddEvent {
      def execute(): RIO[BookDB, Event[AppType]] = for {
        bookdb <- ZIO.service[BookDB]
        _ <- bookdb.removeAll(bookMap("name"))
        _ <- ZIO.succeed(bookMap("name") = "")
        _ <- ZIO.succeed(bookMap("author") = "")
        _ <- ZIO.succeed(bookMap("genre") = "")
        _ <- state.set(AddState.Enter("author", false))
        ev <- NAE
      } yield ev
    }

    case class SelectField(up: Boolean) extends AddEvent {
      def execute(): Task[Event[AppType]] = for {
        stateValue <- state.get
        _ <- state.set(
          if (up) {
            stateValue match {
              case AddState.Enter("genre", v) => AddState.Enter("author", v)
              case AddState.Enter(_, v) => AddState.Enter("name", v)
              case s => s
            }
          } else
            {
              stateValue match {
                case AddState.Enter("name", v) => AddState.Enter("author", v)
                case AddState.Enter(_, v) => AddState.Enter("genre", v)
                case s => s
              }
            })
        ev <- NAE
      } yield ev
    }
  }

}

object AddWindow {
  def apply(mode: AppMode): UIO[AddWindow] = for {
    updated <- Ref.make(false)
    state <- Ref.make[AddState](AddState.Enter("name", false))
  } yield AddWindow(updated, state, mode)
}