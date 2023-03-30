package Validator

import DB.{User, UserDB, Users}
import zio.{Ref, ZIO, ZLayer}
import slick.jdbc.H2Profile.api._

class UserValidator(override val db: UserDB,
                    val user: Ref[String],
                    val password: Ref[String],
                    val id: Ref[Int]) extends Validator[User, Users, UserDB] {


  def checkFirst(tag: String): ZIO[Any, Throwable, Boolean] = for {
    users <- db.search(tag)
    result <- users.headOption match {
      case Some(_) => ZIO.succeed(true)
      case None => ZIO.succeed(false)
    }
  } yield result

  def checkSecond(tag: String): ZIO[Any, Throwable, Boolean] = password.get.flatMap(pass => ZIO.succeed(pass == tag))

  def addFirst(tag: String): ZIO[Any, Throwable, Boolean] = for {
    alreadyExist <- checkFirst(tag)
    _ <- if(!alreadyExist) user.get.flatMap(userValue => ZIO.succeed(userValue == tag))
      else ZIO.unit
  } yield alreadyExist

  def addSecond(tag: String): ZIO[Any, Throwable, Boolean] = for {
    isSafe <- testPassword(tag)
    _ <- if(isSafe) for {
      userValue <- user.get
      passwordValue <- password.get
    } yield db.add(User(userValue, passwordValue,0))
      else ZIO.unit// TODO añadir id
  } yield isSafe

  def addBoth(first: String, second: String): ZIO[Any, Throwable, Boolean] = for {
    successful1 <- addFirst(first)
    successful2 <- if(successful1) addSecond(second) else ZIO.succeed(false)
  } yield successful1 && successful2

  def delete(first: String): ZIO[Any, Throwable, Unit] = for {
    successful <- db.removeAll(first)
  } yield successful

  def testPassword(first: String): ZIO[Any, Throwable, Boolean] = ZIO.succeed(true)
}

object UserValidator {
  def layer(conf: String): ZLayer[UserDB, Throwable, UserValidator] = ZLayer {
    for {
      user <- Ref.make("")
      password <- Ref.make("")
      id <- Ref.make(0)
      userdb <- ZIO.service[UserDB]
    } yield new UserValidator(userdb, user, password, id)
  }
}
