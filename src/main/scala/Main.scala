import Controller.Event._
import Mode.{AppMode, Mode, ModeType}
import zio.Console.printLine
import zio._


object Main extends ZIOAppDefault {
  def run = loop(AppMode("ho"))

  //loop method execute a mood and changes between modes
  //basic structure: print, wait for new event, act in response to new event
  def loop[SomeType <: ModeType](zioMode: IO[Throwable,Mode[SomeType]]): ZIO[Any, Any, Unit] = for {
    mode <- zioMode                                                                  // get mode
    f1 <- mode.reprint().repeat(Schedule.spaced(1000.millis)).fork                   // output fiber
    f2 <- mode.catchEvent().repeat(Schedule.forever).fork                            // input fiber
    _ <- mode.actualize().repeatWhileEquals(true)                                  // process events

    _ <- f1.interrupt
    //_ <- f2.interrupt
    _ <- f2.join.onInterrupt(ZIO.debug("paroo")).resurrect.ignore.disconnect.timeout(100.millis)

    //_ <- ZIO.when(timeout.isEmpty)(f2.interruptFork)
    _ <- printLine(mode.continue.get)

    //loop with new mode or end loop
    lastEvent <- ZIO.succeed(mode.lastEvent)
    nextMode <- lastEvent match {
      case lastEvent: ChangeMode[_] => loop(lastEvent.nextMode)
      case _: TerminateEvent[_] => ZIO.unit
    }
  } yield()



}