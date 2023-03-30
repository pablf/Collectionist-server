import App.{AppMode, Profile}
import DB.{BookDB, RatingDB, UserDB}
import Mode.Event._
import Login.LoginMode
import Mode.{Mode, ModeType}
import Recommendation.Recommender
import Validator.UserValidator
import zio._
import org.fusesource.jansi.AnsiConsole


object Main extends ZIOAppDefault {
  def run =
    ZIO.attempt(AnsiConsole.systemInstall()) *>
      ZIO.attempt(new Profile("YO", 0))
        .flatMap(profile => loop(AppMode(profile))
          .provide(
            UserValidator.layer("user"),
            Recommender.layer,
            RatingDB.layer,
            UserDB.layer,
            BookDB.layer("bookdb"))) *>
      ZIO.attempt(AnsiConsole.systemUninstall())


  //def run1 = loop(LoginMode())
/*
  val env: ZEnvironment[UserValidator & Recommender & RatingDB & UserDB & BookDB] =
    ZEnvironment[UserValidator, Recommender, RatingDB, UserDB, BookDB](
      UserValidator.layer("user"),
      Recommender.layer,
      RatingDB.layer,
      UserDB.layer,
      BookDB.layer("bookdb"))

*/


  //loop method execute a mood and changes between modes
  //basic structure: print, wait for new event, act in response to new event
  def loop[SomeType <: ModeType](zioMode: IO[Throwable,Mode[SomeType]]): ZIO[
    UserValidator &
      Recommender &
      RatingDB &
      UserDB &
      BookDB, Any, Unit] = for {
    mode <- zioMode                                                                  // get mode
    f1 <- mode.reprint().repeat(Schedule.spaced(1000.millis)).fork                   // output fiber
    f2 <- mode.catchEvent().repeat(Schedule.forever).fork                            // input fiber
    _ <- mode.actualize().repeatWhileEquals(true)                                    // process events

    _ <- f1.interrupt
    //_ <- f2.interrupt
    _ <- f2.join.resurrect.ignore.disconnect.timeout(100.millis)

    //_ <- ZIO.when(timeout.isEmpty)(f2.interruptFork)
    //_ <- printLine(mode.continue.get)

    //loop with new mode or end loop
    lastEvent <- mode.lastEvent.get
    nextMode <- lastEvent match {
      case lastEvent: ChangeMode[_] => ZIO.unit/*loop(lastEvent.nextMode).provide(
          UserValidator.layer("user"),
          Recommender.layer,
          RatingDB.layer,
          UserDB.layer,
          BookDB.layer("bookdb"))*/
      case _: TerminateEvent[_] => ZIO.unit
    }
  } yield()



}