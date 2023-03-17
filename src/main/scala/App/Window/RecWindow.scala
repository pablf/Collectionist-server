package App
import App.Window.Window
import Mode.Event.NAE
import Mode.{Event, ExEvent, ModeType}
import Mode.ModeType.AppType
import Recommendation.Recommender
import zio.Console.printLine
import zio.{Cause, Task, ZIO, durationInt}

import scala.concurrent.TimeoutException
import java.io.IOException

class RecWindow(val recommender: Recommender, val id: Int) extends Window {
  val name: String = "Recommender"
  var scrollIndex: Int = 0
  val ScrollAmplitude: Int = 3


  def print(): ZIO[Any, IOException, Unit] = for {
    recommendations <- recommender.giveRecommendation(id)
      .timeoutFailCause(Cause.die(new Error("timeout")))(3.second)
      .catchAll {
        _ => printLine("huha") *> ZIO.succeed(Array()).debug("did not finish recommendations")
      }
    _ <- if(recommendations.isEmpty) printLine("Ups, we could not load any recommendations for you...")
    else for{
      _ <- printLine("    You might be interested in the following books")
      _ <- ZIO.foreach(scrollIndex to ScrollAmplitude)(i => zio.Console.print(s"| > ${recommendations(i).name}, by ${recommendations(i).author} |"))
      _ <- printLine("")
      _ <- ZIO.foreach(scrollIndex to ScrollAmplitude)(i => zio.Console.print(s"|     Genre: ${recommendations(i).genre}."))
      _ <- printLine("")
    } yield()
  } yield()

  def printKeymap(): ZIO[Any, IOException, Unit] = ZIO.unit

  def keymap(key: String): Event[ModeType.AppType] = key match {
    case "l" => RecEvent.Scroll(false)
    case "r" => RecEvent.Scroll(true)
  }

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
