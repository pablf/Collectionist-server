package App

import Controller.Event
import Mode._
import Mode.ModeType.AppType
import zio._

import java.io.IOException

trait Window {
  val name: String
  var updated: Boolean = false

  def print(): ZIO[Any, IOException, Unit]
  def printKeymap(): ZIO[Any, IOException, Unit]

  def keymap(key: String): Event[AppType]
}
