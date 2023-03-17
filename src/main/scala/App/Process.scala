package App

import Mode.Event
import Mode.ModeType.AppType
import zio._

import java.io.IOException

abstract class Process(val fiber: ZIO[Any,Throwable, Event[AppType]]) {


  def keymap(tag: String): Event[AppType]

  def print(): ZIO[Console, IOException, Unit]
  def printKeymap(): ZIO[Console, IOException, Unit]

  def process():ZIO[Any, Throwable, Unit]
}
