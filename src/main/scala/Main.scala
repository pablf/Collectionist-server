import Common.{JSON, JWT}
import DB.{BookDB, PlainBookDB, RatingDB, UserDB}
import Recommendation.Recommender
import Spelling.SpellingChecker
import Validator.UserValidator
import zio.{&, Task, ZIO, ZIOAppDefault}
import zio.http.{!!, ->, /, Http, HttpApp, Method, Request, Response, Server}
import zio.http.HttpAppMiddleware

/*
 * ZIOApp with REST API. Composed with 3 REST APIs: login, book and recommender.
 *
 * - login use a UserValidator and checks if a user does exists, checks if some user name or password is usable, returns
 * a JWT token if a user and password are correct, changes passwords, finds the id of an user, creates new users and delete
 * old ones.
 *    Routes:
 *      - "/exist/" + tag
 *      - "/checks/user?/" + tag
 *      - "/checks/password?/" + tag
 *      - "/login/" + user + "/" + password
 *      - "/changepassword/" + user + "/" + password
 *      - "/deleteaccount/" + user
 *      - "/login/id?/" + user
 *      - "/login/" + user + "/" + password
 *
 * - book is divided by permissionLevel using auth(permissionLevel) middleware. bookn has middleware auth(n).
 *
 * - recommender responds with books given by the Recommender services.
 *    Routes:
 *      - "/recommendation?/" + id,  where id is the id of the user that needs recommendations.
 */

object Main extends ZIOAppDefault {

  //Server.Config.default is (None,0.0.0.0/0.0.0.0:8080,false,true,No,None,Disabled(102400),8192,true,PT10S)
  lazy val run: Task[Unit] = Server.serve(app.withDefaultErrorResponse).provide(
    Server.default,
    UserValidator.layer,
    RatingDB.layer,
    UserDB.layer,
    BookDB.layer,
    SpellingChecker.layer)

  lazy val app: HttpApp[
    UserValidator &
      RatingDB &
      UserDB &
      BookDB &
      SpellingChecker, Throwable] = login ++
    book0 @@ auth(0) ++
    book100 @@ auth(100) ++
    book200 @@ auth(200) ++
    book300 @@ auth(300) ++
    recommender.provideLayer(Recommender.layer)



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

  val book0: HttpApp[BookDB, Throwable] =
    Http.collectZIO[Request] {

      case Method.GET -> !! / "db" / "book" / "addTo" / list / userId / bookId =>
        for {
          bookdb <- ZIO.service[BookDB]
          book <- bookdb.find(bookId.toInt)
          listdb <- PlainBookDB(userId + "/" + list)
          _ <- ZIO.foreach(book)(b => listdb.add(b.toPlainBook))
        } yield Response.ok

      case Method.GET -> !! / "db" / "book" / "removeFrom" / list / userId / bookId =>
        for {
          listdb <- PlainBookDB(userId + "/" + list)
          _ <- listdb.delete(bookId.toInt)
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

  val book100: HttpApp[BookDB, Throwable] =
    Http.collectZIO[Request] {

      case Method.POST -> !! / "db" / "book" / "reserve" / user / book =>
        for {
          bookdb <- ZIO.service[BookDB]
          _ <- bookdb.reserve(user.toInt, book.toInt)
        } yield Response.ok

      case Method.POST -> !! / "db" / "book" / "renew" / user / id =>
        for {
          bookdb <- ZIO.service[BookDB]
          _ <- bookdb.renew(id.toInt)
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

  val book200: HttpApp[UserDB & BookDB, Throwable] =
    Http.collectZIO[Request] {

      case Method.POST -> !! / "db" / "book" / "return" / user / id =>
        for {
          bookdb <- ZIO.service[BookDB]
          userdb <- ZIO.service[UserDB]
          daysLate <- bookdb.returnBook(id.toInt)
          _ <- ZIO.when(daysLate > 0){
            userdb.ban(user.toInt, daysLate)
          }
        } yield Response.ok

      case Method.POST -> !! / "db" / "book" / "add" / book => for {
        bookdb <- ZIO.service[BookDB]
        _ <- bookdb.add(JSON.decodeBook(book))
      } yield Response.ok

      case Method.DELETE -> !! / "db" / "book" / name =>
        for {
          bookdb <- ZIO.service[BookDB]
          _ <- bookdb.removeAll(name)
        } yield Response.ok
    }

  val book300: HttpApp[BookDB, Throwable] =
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

  def auth(permissionLevel: Int): HttpAppMiddleware[Nothing, UserValidator &
    RatingDB &
    UserDB &
    BookDB &
    SpellingChecker, Throwable, Any] =
    HttpAppMiddleware.allowZIO(request => JWT.isValid(permissionLevel, request.body.asString))


}