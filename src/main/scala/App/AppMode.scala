package App

import Mode.Event._
import DB._
import Exit.ExitMode
import Mode.ModeType.{AppType, ExitType}
import Mode.{Event, Mode}
import Window.{AddWindow, SearchWindow, Window}
import Recommendation.Recommender
import zio.Console.printLine
import zio.{IO, Ref, Task, UIO, ZIO, durationInt}

import java.io.IOException
import scala.collection.mutable.ArrayBuffer

/*
The AppMode is the library manager itself. It serves as a hub for different functionalities given by subclasses of App.Window
There are AddWindow, ConfigurationWindow, RemoveWindow, RecWindow and SearchWindow.
 */

case class AppMode(val user: Profile,
                   override val eventQueue: zio.Queue[Event[AppType]],
                   override val mustReprint: zio.Ref[Boolean],
                   override val continue: Ref[Boolean],
                   val bookdb: BookDB,
                   val ratingsdb: RatingDB,
                   windowsRef: Ref[ArrayBuffer[Window]],
                   val mainWindow: Ref[Int],
                   val recWindow: Ref[Option[RecWindow]]) extends Mode[AppType] {
  var updated: Boolean = false
  var recommenderIsLoaded: Boolean = false



  //print routine...
  def print(): ZIO[Any, IOException, Unit] = for {
    n <- mainWindow.get
    recommendation <- recWindow.get
    windows <- windowsRef.get

    _ <- header()
    _ <- if(n == -1) emptyWindow()
        else windows(n).print()
    _ <- recommendation match {
      case None => printLine("Waiting...")
      case Some(window) => window.print()
        .timeoutFail(new Throwable)(10.millis)
        .catchAll(_ => printLine("Something failed..."))
    }
    _ <- tabs(windows, n)
    _ <- if(n > -1) windows(n).printKeymap() else ZIO.unit
    _ <- standardKeymap()
  } yield ()


  //submethods for printing parts
  def header(): ZIO[Any, IOException, Unit] =
    printLine(s"Welcome to the Collectionist, ${user.name}!") //put type of user???

  def emptyWindow(): IO[IOException, Unit] =
    printLine("    Welcome to Collectionist, the library manager") *>
      printLine("    Select a task to start!") *> printLine("")

  def tabs(windows: ArrayBuffer[Window], main: Int): ZIO[Any, IOException, Unit] = if(windows.nonEmpty) {
    if(main == 0) zio.Console.print("| " + ">> " + windows.head.name + "    |")
    else if(windows.head.updated) zio.Console.print("| " + "  " + windows.head.name + " !! |")
    else zio.Console.print("| " + "  " + windows.head.name + "    |")
  } *> tabs(windows.tail, main - 1)
  else printLine("")


  def standardKeymap(): ZIO[Any, IOException, Unit] =
    printLine("Search [S]      Manage library [M]      Profile configuration [C]      Quit window [Q]      Exit [E]")




  // methods for events
  def command(tag: String): UIO[Event[AppType]] = {
    tag match {
      case Int(_) | "S" | "M" | "C" | "Q" | "E" => ZIO.succeed(keymap(tag))
      case _ => for {
        n <- mainWindow.get
        windows <- windowsRef.get
      } yield windows(n).keymap(tag)
    }
  }

  def keymap(tag: String): Event[AppType] = tag match {
    case Int(n) => AppEvent.GotoProcess(n)
    case "S" => AppEvent.Open(new SearchWindow(this))
    case "M" => AppEvent.Open(new AddWindow(this))
    case "C" => AppEvent.OpenZIO(ConfigurationWindow(this))
    case "Q" => AppEvent.QuitWindow()
    case "E" => ToExit()
  }

  object Int {
    def unapply(s: String): Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }


  /*
  AppEvent object describes the Events for a instance of AppMode
    GotoProcess, QuitWindow, OpenZIO, Open, Update, Loader
    OpenZIO and Open both open a new windows, but in OpenZIO the window is passed as a parameter wrapped in ZIO

  The object AppEvent inside of the class AppMode avoids Dependency Injection and allows directly modifying the class AppMode
   */
  object AppEvent {
    final case class GotoProcess(newWindow: Int) extends AppEvent {
      def execute(): Task[Event[AppType]] = for {
        windows <- windowsRef.get
        _ <- if(newWindow <= windows.length) mainWindow.set(newWindow - 1) else ZIO.unit
        n <- mainWindow.get
      } yield Update(n, false)
    }

    final case class QuitWindow() extends AppEvent {
      def execute(): Task[Event[AppType]] = for {
        n <- mainWindow.get
        _ <- windowsRef.update(windows => {
          windows.remove(n)
          windows
        })
        _ <- ZIO.when(n > 0)(mainWindow.set(n - 1))
      } yield Update(n - 1, false)
    }

    final case class OpenZIO(windowZIO: IO[Throwable,Window]) extends AppEvent {
      def execute(): Task[Event[AppType]] = for {
        n <- mainWindow.get
        window <- windowZIO
        _ <- windowsRef.update(_.addOne(window))
        windows <- windowsRef.get
        _ <- mainWindow.set(windows.length - 1)
      } yield Update(n, false)
    }

    final case class Open(window: Window) extends AppEvent {
      def execute(): Task[Event[AppType]] = for {
        n <- mainWindow.get
        _ <- windowsRef.update(_.addOne(window))
        windows <- windowsRef.get
        _ <- mainWindow.set(windows.length - 1)
      } yield Update(n, false)
    }

    final case class Update(n: Int, isUpdated: Boolean) extends AppEvent {
      def execute(): Task[Event[AppType]] = for {
        _ <- if(n > -1) windowsRef.update(windows => {
          windows(n).updated = false
          windows
        }) else ZIO.unit
        ev <- NAE
      } yield ev
    }

    /*
     Event Loader() is needed to load Spark and the Recommendation model after the creation of AppMode concurrently
     allowing the use of the application while Spark is not loaded.
     */

    case class Loader() extends Load[AppType] {
      def execute(): Task[Event[AppType]] = for {
        recommender <- ZIO.succeed(new Recommender(bookdb))
        newRecWindow <- ZIO.succeed(new RecWindow (recommender, user.id))
        _ <- recWindow.set(Some(newRecWindow))
        ev <- NAE
      } yield ev
    }

  }

  case class ToExit() extends ChangeMode[AppType] {
    type nextType = ExitType
    val nextMode = ExitMode()
  }



}

object AppMode{
  def apply(user: Profile): IO[Throwable, AppMode] = {
    for {
      ref <- Ref.make(true)
      continue <- Ref.make(true)
      queue <- zio.Queue.unbounded[Event[AppType]]
      bookdb <-  BookDB("a", "bookdb")
      ratingsdb <- RatingDB()
      windowsRef <- Ref.make(ArrayBuffer[Window]())
      mainWindow <- Ref.make(0)
      recWindow <- Ref.make[Option[RecWindow]](None)
      appMode <- ZIO.succeed(AppMode(user, queue, ref, continue, bookdb, ratingsdb, windowsRef, mainWindow, recWindow))

      //Loads a new SearchWindow as predefault. TODO make this depend of user configuration
      _ <- appMode.windowsRef.update(_.addOne(SearchWindow(appMode)))

      // Event Loader is added to the Queue of events to load spark and the recommender concurrently
      //_ <- appMode.eventQueue.offer(appMode.AppEvent.Loader())
    } yield appMode
  }
}

