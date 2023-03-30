package App.Window

import App.AppMode
import DB.{Book, BookDB}
import Mode.Event.NAE
import Mode.{Event, ExEvent}
import Mode.ModeType.AppType
import zio.Console.printLine
import zio.{RIO, Ref, Task, UIO, ZIO}

import java.io.IOException
import scala.collection.mutable.ListBuffer

case class SearchWindow(override val updated: Ref[Boolean],
                        override val state: Ref[SearchState],
                        mode: AppMode) extends Window[SearchState] {
  val name = "Search"

  var itemsAS: ListBuffer[String] = ListBuffer()
  var tagsAS: ListBuffer[String] = ListBuffer()

  // the tag of a search indicates the parameter????
  val possibleTags: Array[String] = Array("Name", "Author", "Genre", "Id")
  var currentTag: Int = 0

  var currentBook: Option[Book] = None
  var currentBooks: List[Book] = List()

  def print(): ZIO[Any, IOException, Unit] = for {
    stateValue <- state.get
  } yield stateValue match
    {
      case SearchState.SimpleSearch() => printLine("   To make a simple search, enter some term.")
      case SearchState.AdvancedSearch() => printLine("   Select the tag (up [u] or down [d]) and enter some term.")
      case SearchState.NotFound() => printLine("I am sorry. We could not find your book. Search another one!")
      case SearchState.Found() => printLine("Congratulations! Here are the books:") *> printBooks()
    }

  def printKeymap(): ZIO[Any, IOException, Unit] = for {
    stateValue <- state.get
  } yield stateValue match {
      case SearchState.SimpleSearch() => printLine("   To [A]dvanced search.")
      case SearchState.AdvancedSearch() => printLine("    [G]o to search. To [Si]mple search.")
      case _ => printLine("Make a new [A]dvanced search or [Si]mple search.")
    }

  def printBooks(): ZIO[Any, IOException, Unit] = for {
    _ <- ZIO.foreach(currentBooks)(book => printLine(s" ->    ${book.name}, by ${book.author}") *> printLine(s" Genre: ${book.genre}"))
  } yield ()


  def keymap(tag: String): UIO[Event[AppType]] = tag match {
    case "A" => ZIO.succeed(SearchEvent.ChangeState(true, SearchState.AdvancedSearch(), SearchState.AdvancedSearch()))
    case "Si" => ZIO.succeed(SearchEvent.ChangeState(true, SearchState.SimpleSearch(), SearchState.AdvancedSearch()))
    case tag => for {
      stateValue <- state.get
      ev <- stateValue match {
        case SearchState.SimpleSearch() => ZIO.succeed(SearchEvent.SimpleSearch(tag))
        case SearchState.AdvancedSearch() => ZIO.succeed(tag match {
          case "d" => SearchEvent.ChangeUp(false)
          case "u" => SearchEvent.ChangeUp(true)
          case "G" => SearchEvent.AdvancedSearch()
          case _ => SearchEvent.AddToAdvancedSearch(tag)
        })
        case _ => ZIO.succeed(SearchEvent.SimpleSearch(tag))
      }
    } yield ev



  }



  trait SearchEvent extends ExEvent[AppType]

  object SearchEvent {


    final case class ChangeState(condition: Boolean,
                                 trueState: SearchState,
                                 falseState: SearchState ) extends SearchEvent {

      def execute(): Task[Event[AppType]] = {
        if (condition) state.set(trueState) else state.set(falseState)
        NAE
      }
    }

    final case class SimpleSearch(item: String) extends SearchEvent {
      def execute(): RIO[BookDB, Event[AppType]] = for {
        bookdb <- ZIO.service[BookDB]
        books <- bookdb.generalSearch(item)
        _ <- if (books.nonEmpty) ZIO.succeed({
          currentBook = Option(books.head)
          currentBooks = books
        }) else ZIO.unit
        _ <- updated.set(true)
      } yield ChangeState(books.nonEmpty, SearchState.Found(), SearchState.NotFound())
    }

    final case class AdvancedSearch() extends SearchEvent {
      def execute(): RIO[BookDB, Event[AppType]] = for {
        bookdb <- ZIO.service[BookDB]
        books <- bookdb.advancedSearch(itemsAS.toList, tagsAS.toList)
        _ <- if (books.nonEmpty) ZIO.succeed({
          currentBook = Option(books.head)
          currentBooks = books
        }) else ZIO.unit
        _ <- updated.set(true)
      } yield ChangeState(books.nonEmpty, SearchState.Found(), SearchState.NotFound())
    }


    final case class AddToAdvancedSearch(item: String) extends SearchEvent {
      def execute(): Task[Event[AppType]] = {
        itemsAS.addOne(item)
        tagsAS.addOne(possibleTags(currentTag))
        NAE
      }
    }


    final case class ChangeUp(up: Boolean) extends SearchEvent {
      def execute(): Task[Event[AppType]] = {
        if (up) {
          if (currentTag < possibleTags.length) currentTag = currentTag + 1
        } else {
          if (currentTag > 0) currentTag = currentTag - 1
        }
        NAE
      }
    }

  }


}

object SearchWindow {
  def apply(mode: AppMode): UIO[SearchWindow] = for {
    updated <- Ref.make(false)
    state <- Ref.make[SearchState](SearchState.SimpleSearch())
  } yield SearchWindow(updated, state, mode)
}
