package App

import Mode.Event._
import DB._
import Exit.ExitMode
import Mode.ModeType.{AppType, ExitType}
import Mode.{Event, Mode}
import Window.{AddWindow, SearchWindow, Window, WindowState}
import Recommendation.Recommender
import Validator.UserValidator
import zio.Console.printLine
import zio.{&, IO, RIO, Ref, Task, UIO, ZIO, ZLayer, durationInt}

import java.io.IOException
import scala.collection.mutable.ArrayBuffer

/*
The AppMode is the library manager itself. It serves as a hub for different functionalities given by subclasses of App.Window
There are AddWindow, ConfigurationWindow, RemoveWindow, RecWindow and SearchWindow.
 */

case class AppMode(user: Profile,
                   override val eventQueue: zio.Queue[Event[AppType]],
                   override val mustReprint: zio.Ref[Boolean],
                   override val continue: Ref[Boolean],
                   override val lastEvent: Ref[Event[AppType]],
                   windowsRef: Ref[ArrayBuffer[Window[_]]],
                   mainWindow: Ref[Int],
                   recWindow: Ref[Option[RecWindow]]) extends Mode[AppType] {
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
      case None => printLine("    Loading recommendations...")
      case Some(window) => window.print()
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

  def tabs(windows: ArrayBuffer[Window[_]], main: Int): ZIO[Any, IOException, Unit] = if(windows.nonEmpty) {
    if(main == 0) zio.Console.print("| " + ">> " + windows.head.name + "    |")
    else ZIO.ifZIO(windows.head.updated.get)(
      onTrue = zio.Console.print("| " + "  " + windows.head.name + " !! |"),
      onFalse = zio.Console.print("| " + "  " + windows.head.name + "    |")
    )
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
        ev <- windows(n).keymap(tag)
      } yield ev
    }
  }

  def keymap(tag: String): Event[AppType] = tag match {
    case Int(n) => AppEvent.GotoProcess(n)
    case "S" => AppEvent.OpenZIO(SearchWindow(this))
    case "M" => AppEvent.OpenZIO(AddWindow(this))
    case "C" => AppEvent.OpenZIO(ConfigurationWindow(this).provide(UserDB.layer, UserValidator.layer("users")))
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

    final case class OpenZIO(
                              windowZIO: ZIO[
                                Any,
                                Throwable, Window[_]]
                            ) extends AppEvent {
      def execute(): Task[Event[AppType]] = for {
        n <- mainWindow.get
        window <- windowZIO
        _ <- windowsRef.update(_.addOne(window))
        windows <- windowsRef.get
        _ <- mainWindow.set(windows.length - 1)
      } yield Update(n, false)
    }

    final case class Open(window: Window[_]) extends AppEvent {
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
          windows(n).updated.set(false)
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
      def execute(): RIO[BookDB, Event[AppType]] = makeRecWindow.provideSome(Recommender.layer)


      def makeRecWindow(): RIO[Recommender, Event[AppType]] = for {
        recommender <- ZIO.service[Recommender]
        emptyList <- Ref.make[Option[Array[Book]]](None)
        newRecWindow <- ZIO.succeed(new RecWindow(recommender, user.id, emptyList))
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
      queue <- zio.Queue.unbounded[Event[AppType]]
      ref <- Ref.make(true)
      continue <- Ref.make(true)
      lastEvent <- Ref.make[Event[AppType]](new TerminateEvent[AppType])


      windowsRef <- Ref.make(ArrayBuffer[Window[_]]())
      mainWindow <- Ref.make(0)
      recWindow <- Ref.make[Option[RecWindow]](None)
      appMode <- ZIO.succeed(AppMode(user, queue, ref, continue, lastEvent, windowsRef, mainWindow, recWindow))

      //Loads a new SearchWindow as predefault. TODO make this depend of user configuration
      window <- SearchWindow(appMode)
      _ <- appMode.windowsRef.update(_.addOne(window))

      // Event Loader is added to the Queue of events to load spark and the recommender concurrently
      _ <- appMode.eventQueue.offer(appMode.AppEvent.Loader())
    } yield appMode
  }
}

