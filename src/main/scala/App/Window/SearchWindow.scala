package App.Window

import App.AppMode
import DB.Book
import Mode.Event.NAE
import Mode.{Event, ExEvent}
import Mode.ModeType.AppType
import zio.Console.printLine
import zio.{Task, ZIO}

import java.io.IOException
import scala.collection.mutable.ListBuffer

case class SearchWindow(val mode: AppMode) extends Window {
  val name = "Search"
  var state: SearchState = SearchState.SimpleSearch()

  var itemsAS: ListBuffer[String] = ListBuffer()
  var tagsAS: ListBuffer[String] = ListBuffer()

  // the tag of a search indicates the parameter????
  val possibleTags: Array[String] = mode.bookdb.possibleTags
  var currentTag: Int = 0

  var currentBook: Option[Book] = None
  var currentBooks: List[Book] = List()

  def print(): ZIO[Any, IOException, Unit] = state match {
    case SearchState.SimpleSearch() => printLine("   To make a simple search, enter some term.")
    case SearchState.AdvancedSearch() => printLine("   Select the tag (up [u] or down [d]) and enter some term.")
    case SearchState.NotFound() => printLine("I am sorry. We could not find your book. Search another one!")
    case SearchState.Found() => printLine("Congratulations! Here are the books:") *> printBooks()
  }

  def printKeymap(): ZIO[Any, IOException, Unit] = state match {
    case SearchState.SimpleSearch() => printLine("   To [A]dvanced search.")
    case SearchState.AdvancedSearch() => printLine("    [G]o to search. To [Si]mple search.")
    case _ => printLine("Make a new [A]dvanced search or [Si]mple search.")
  }

  def printBooks(): ZIO[Any, IOException, Unit] = for {
    _ <- ZIO.foreach(currentBooks)(book => printLine(s" ->    ${book.name}, by ${book.author}") *> printLine(s" Genre: ${book.genre}"))
  } yield ()


  def keymap(tag: String): Event[AppType] = tag match {
    case "A" => SearchEvent.ChangeState(true, SearchState.AdvancedSearch())
    case "Si" => SearchEvent.ChangeState(true, SearchState.SimpleSearch())
    case tag => state match {
      case SearchState.SimpleSearch() => SearchEvent.SimpleSearch(tag)
      case SearchState.AdvancedSearch() => tag match {
        case "d" => SearchEvent.ChangeUp(false)
        case "u" => SearchEvent.ChangeUp(true)
        case "G" => SearchEvent.AdvancedSearch()
        case _ => SearchEvent.AddToAdvancedSearch(tag)
      }
      case _ => SearchEvent.SimpleSearch(tag)
    }
  }



  trait SearchEvent extends ExEvent[AppType]

  object SearchEvent {


    final case class ChangeState(condition: Boolean,
                                 trueState: SearchState,
                                 falseState: SearchState = state ) extends SearchEvent {

      def execute(): Task[Event[AppType]] = {
        if (condition) state = trueState else state = falseState
        NAE
      }
    }

    final case class SimpleSearch(item: String) extends SearchEvent {
      def execute(): Task[Event[AppType]] = for {
        books <- mode.bookdb.generalSearch(item)
        _ <- if (books.nonEmpty) ZIO.succeed({
          currentBook = Option(books.head)
          currentBooks = books
          updated = true
        }) else ZIO.unit
      } yield ChangeState(books.nonEmpty, SearchState.Found(), SearchState.NotFound())
    }

    final case class AdvancedSearch() extends SearchEvent {
      def execute(): Task[Event[AppType]] = for {
        books <- mode.bookdb.advancedSearch(itemsAS.toList, tagsAS.toList)
        _ <- if (books.nonEmpty) ZIO.succeed({
          currentBook = Option(books.head)
          currentBooks = books
          updated = true
        }) else ZIO.unit
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
