import Common.JSON
import DB.{BookDB, RatingDB, UserDB}
import Recommendation.Recommender
import Validator.UserValidator
import zio.{&, UIO, ZIO, ZIOAppDefault, Task}
import zio.http.{!!, ->, /, Http, HttpApp, Method, Request, Response, Server}

object Main extends ZIOAppDefault {

  //Server.Config.default is (None,0.0.0.0/0.0.0.0:8080,false,true,No,None,Disabled(102400),8192,true,PT10S)
  lazy val run: Task[Unit] = Server.serve(app.withDefaultErrorResponse).provide(
    Server.default,
    UserValidator.layer("user"),
    RatingDB.layer,
    UserDB.layer,
    BookDB.layer("bookdb"))

  lazy val app: HttpApp[
    UserValidator &
      RatingDB &
      UserDB &
      BookDB, Throwable] = login ++ book ++ recommender.provideLayer(Recommender.layer)



  // Values login, book and recommender. They are HttpApps that compose val app

  val login: HttpApp[UserValidator, Throwable] = Http.collectZIO[Request] {

    // does user exist?
    case Method.GET -> !! / "exist" / tag => for {
      validator <- ZIO.service[UserValidator]
      exist <- validator.existFirst(tag)
    } yield Response.text(exist.toString)

    // is tag a valid username?
    case Method.GET -> !! / "check" / "user?" / tag => for {
      validator <- ZIO.service[UserValidator]
      isValid <- validator.checkFirst(tag)
    } yield Response.text(isValid.toString)

    // is tag a valid password?
    case Method.GET -> !! / "check" / "password?" / tag => for {
      validator <- ZIO.service[UserValidator]
      isValid <- validator.checkSecond(tag)
    } yield Response.text(isValid.toString)

    // are user and password correct?
    case Method.GET -> !! / "login" / user / password => for {
      validator <- ZIO.service[UserValidator]
      isValid <- validator.tryLogin(user, password)
    } yield Response.text(isValid.toString)

    // are user and password correct?
    case Method.GET -> !! / "changepassword" / user / password => for {
      validator <- ZIO.service[UserValidator]
      isValid <- validator.change(user, password)
    } yield Response.text(isValid.toString)

    case Method.GET -> !! / "deleteaccount" / user => for {
      validator <- ZIO.service[UserValidator]
      _ <- validator.delete(user)
    } yield Response.ok

    // which is the id of an user
    case Method.GET -> !! / "login" / "id?" / user => for {
      validator <- ZIO.service[UserValidator]
      id <- validator.id(user)
    } yield Response.text(id.toString)

    // add new user
    case Method.GET -> !! / "signup" / user / password => for {
      validator <- ZIO.service[UserValidator]
      id <- validator.add(user, password)
    } yield Response.text(id.toString)

  }

  val book: HttpApp[BookDB, Throwable] =
    Http.collectZIO[Request] {

      case Method.POST -> !! / "db" / "book" / "add" / book => for {
        bookdb <- ZIO.service[BookDB]
        _ <- bookdb.add(JSON.decodeBook(book))
      } yield Response.ok

      case Method.DELETE -> !! / "db" / "book" / name =>
        for {
          bookdb <- ZIO.service[BookDB]
          _ <- bookdb.removeAll(name)
        } yield Response.ok

      case Method.GET -> !! / "db" / "book" / "search" / tag =>
        for {
          bookdb <- ZIO.service[BookDB]
          result <- bookdb.generalSearch(tag)
        } yield Response.json(JSON.encodeBooks(result))

      case Method.GET -> !! / "db" / "book" / "advancedsearch" / itemsAS / tagsAS =>
        for {
          bookdb <- ZIO.service[BookDB]
          result <- bookdb.advancedSearch(JSON.decodeStrings(itemsAS).toList, JSON.decodeStrings(tagsAS).toList)
        } yield Response.json(JSON.encodeBooks(result))

    }

  val recommender: HttpApp[Recommender, Throwable] = Http.collectZIO[Request] {

    // obtains recommendations for user with ID id
    case Method.GET -> !! / "recommendation?" / id => for {
      recommender <- ZIO.service[Recommender]
      result <- recommender.giveRecommendation(id.toInt)
    } yield Response.json(JSON.encodeBooks(result))

  }

}