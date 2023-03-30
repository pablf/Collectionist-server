package App.Window

import Mode.Event
import Mode.ModeType.AppType
import zio.{Ref, UIO, ZIO}

import java.io.IOException

trait Window[WState <: WindowState] {
  val name: String
  val updated: Ref[Boolean]
  val state: Ref[WState]

  def print(): ZIO[Any, IOException, Unit]
  def printKeymap(): ZIO[Any, IOException, Unit]

  def keymap(key: String): UIO[Event[AppType]]
}
