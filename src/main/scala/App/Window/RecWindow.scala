package App
import App.Window.{Window, WindowState}
import Mode.Event.NAE
import Mode.{Event, ExEvent, ModeType}
import Mode.ModeType.AppType
import Recommendation.Recommender
import zio.Console.printLine
import zio.{Cause, Task, UIO, ZIO, durationInt, Ref}

import DB.Book

import scala.concurrent.TimeoutException
import java.io.IOException


class RecWindow(val recommender: Recommender, val id: Int, val recommendations: Ref[Option[Array[Book]]]) {
  val name: String = "Recommender"
  var scrollIndex: Int = 0
  val ScrollAmplitude: Int = 3

  def actualizeRecommendations: ZIO[Any, Throwable, Unit] = for {
    newRecommendations <- recommender.giveRecommendation(id)
      _ <- recommendations.set(Some(newRecommendations))
  } yield ()





  def print(): ZIO[Any, IOException, Unit] = for {
    _ <- actualizeRecommendations.fork
      //.timeoutFailCause(Cause.die(new Error("timeout")))(3.second)
      //.catchAll {
        //_ => printLine("huha") *> ZIO.succeed(Array()).debug("did not finish recommendations")
      //}
    r <- recommendations.get
    _ <- r match {
      case None => printLine("    Loading recommendations...")
      case Some(rec) => {
        if (rec.isEmpty) printLine("    Ups, we could not load any recommendations for you...")
        else for {
          _ <- printLine("    You might be interested in the following books:")
          texts <- ZIO.succeed(rec.map(book => Array(s" > ${book.name}, by ${book.author} ", s"     Genre: ${book.genre}")).map(format(_)))
          _ <- zio.Console.print("  ")
          _ <- ZIO.foreach(texts)(text => zio.Console.print(text(0)))
          _ <- printLine("")
          _ <- zio.Console.print("  ")
          _ <- ZIO.foreach(texts)(text => zio.Console.print(text(1)))
          _ <- printLine("")
          /*
      _ <- ZIO.foreach(rec)(book => zio.Console.print( s"| > ${book.name}, by ${book.author} |"))

      _ <- printLine("")
      //_ <- ZIO.foreach(Range(scrollIndex, ScrollAmplitude))(i => zio.Console.print(s"|     Genre: ${rec(i).genre}."))
      _ <- ZIO.foreach(rec)(book => zio.Console.print(s"|     Genre: ${book.genre}."))

      _ <- printLine("")*/
        } yield ()
      }
    }
  } yield()

  def format(texts: Array[String]): Array[String] = {
    val maxLength = texts.map(_.length).max
    texts.map(text => "|" + text + nSpace(maxLength - text.length)+ "|")
  }

  def nSpace(n: Int): String = if(n < 1) "" else " " + nSpace(n - 1)

  def printKeymap(): ZIO[Any, IOException, Unit] = ZIO.unit

  def keymap(key: String): UIO[Event[ModeType.AppType]] = ZIO.succeed(key match {
    case "l" => RecEvent.Scroll(false)
    case "r" => RecEvent.Scroll(true)
  })

  trait RecEvent extends ExEvent[AppType]

  object RecEvent {
    final case class Scroll(right: Boolean) extends RecEvent {
      def execute(): Task[Event[AppType]] = {
        if(right) scrollIndex = scrollIndex + 1
        else (if(scrollIndex > 0) scrollIndex = scrollIndex - 1 else scrollIndex = 0)
        NAE
      }
    }
  }

}
