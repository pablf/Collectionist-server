package App

import Controller.{Event, ExEvent}
import Controller.Event.NullEvent
import Mode.AppMode
import Mode.ModeType.AppType
import State.SearchState
import zio.Console.printLine
import zio.ZIO

import java.io.IOException

case class SearchWindow(val mode: AppMode) extends Window {
  val name = "Search"
  var state: SearchState = SearchState.Search()

  var currentBook: Option[Book] = None
  var currentBooks: List[Book] = List()

  def print(): ZIO[Any, IOException, Unit] = state match {
    case SearchState.Search() => printLine("Enter a book name to search.")
    case SearchState.NotFound() => printLine("I am sorry. We could not find your book. Search another one!")
    case SearchState.Found() => printLine("Congratulations! Here are the books:") *> printBooks()
  }

  def printKeymap(): ZIO[Any, IOException, Unit] = printLine("asdkj")

  def printBooks(): ZIO[Any, IOException, Unit] = for {
    _ <- ZIO.foreach(currentBooks)(book => printLine(s" ->    ${book.name}, by ${book.author}") *> printLine(s" Genre: ${book.genre}"))
  } yield ()



  def keymap(tag: String): Event[AppType] = SearchEvent.SearchBook(tag)


  trait SearchEvent extends ExEvent[AppType]
  val NAE = NullEvent[AppType]()

  object SearchEvent {
    final case class SearchBook(tag: String) extends SearchEvent {
      def execute(): Event[AppType] = {
        val books = mode.bookdb.search(tag)
        if (books.nonEmpty) {
          currentBook = Option(books.head)
          currentBooks = books
          updated = true
          state = SearchState.Found()
          NAE
        } else NotFoundBook(tag)
      }
    }

    final case class NotFoundBook(tag: String) extends SearchEvent {
      def execute(): Event[AppType] = {
        state = SearchState.NotFound()
        updated = true
        NAE
      }

    }

  }


}
