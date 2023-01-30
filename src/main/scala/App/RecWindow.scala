package App
import Controller.Event.NAE
import Controller.{Event, ExEvent}
import Mode.ModeType
import Mode.ModeType.AppType
import Recommendation.Recommender
import zio.Console.printLine
import zio.ZIO

import java.io.IOException

class RecWindow(val recommender: Recommender) extends Window{
  val name: String = "Recommender"
  var scrollIndex: Int = 0
  val ScrollAmplitude: Int = 3


  def print(): ZIO[Any, IOException, Unit] = for {
    recommendations <- ZIO.succeed(recommender.recommendations)
    _ <- printLine("    You might be interested in the following books")
    _ <- ZIO.foreach(scrollIndex to ScrollAmplitude)(i => zio.Console.print(s"| > ${recommendations(i).name}, by ${recommendations(i).author} |"))
    _ <- printLine("")
    _ <- ZIO.foreach(scrollIndex to ScrollAmplitude)(i => zio.Console.print(s"|     Genre: ${recommendations(i).genre}."))
    _ <- printLine("")
  } yield()

  def printKeymap(): ZIO[Any, IOException, Unit] = ???

  def keymap(key: String): Event[ModeType.AppType] = key match {
    case "l" => RecEvent.Scroll(false)
    case "r" => RecEvent.Scroll(true)
  }

  trait RecEvent extends ExEvent[AppType]

  object RecEvent {
    final case class Scroll(right: Boolean) extends RecEvent {
      def execute(): Event[AppType] = {
        if(right) scrollIndex = scrollIndex + 1
        else (if(scrollIndex > 0) scrollIndex = scrollIndex - 1 else scrollIndex = 0)
        NAE
      }
    }
  }

}
