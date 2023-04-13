package Validator

import Common.User
import DB.{UserDB, Users}
import zio.{ZIO, ZLayer}

/*
 * A service that takes care of authentication of the user. It does:
 *    - check if an user or password is allowed,
 *    - create and delete users,
 *    - check if an user exist,
 *    - obtain the id of an user
 *
 * To change the forbidden user or password names, change notAllowerUsers and notAllowedPasswords
 *
 * The companion object construct a ZLayer that is invoked in Main.scala
 */

class UserValidator(override val db: UserDB) extends Validator[User, Users, UserDB] {

  val notAllowedUsers: List[String] = List()

  val notAllowedPasswords: List[String] = List()

  override def existFirst(tag: String): ZIO[Any, Throwable, Boolean] = for {
    users <- db.search(tag)
    result <- users.headOption match {
      case Some(_) => ZIO.succeed(true)
      case None => ZIO.succeed(false)
    }
  } yield result

  override def checkFirst(tag: String): ZIO[Any, Throwable, Boolean] = ZIO.succeed(notAllowedUsers.contains(tag))

  override def checkSecond(tag: String): ZIO[Any, Throwable, Boolean] = ZIO.succeed(notAllowedPasswords.contains(tag))

  override def tryLogin(fst: String, snd: String): ZIO[Any, Throwable, Boolean] = for {
    users <- db.search(fst)
    result <- users.headOption match {
      case Some(user) => ZIO.succeed(user.password == snd)
      case None => ZIO.succeed(false)
    }
  } yield result

  override def add(fst: String, snd: String): ZIO[Any, Throwable, Boolean] =
    db.add(User(fst, snd,0)) *> ZIO.succeed(true)

  override def change(fst: String, snd: String): ZIO[Any, Throwable, Boolean] =
    db.update(User(fst, snd,0)) *> ZIO.succeed(true)

  override def id(fst: String): ZIO[Any, Throwable, Int] = for {
    users <- db.search(fst)
    result <- users.headOption match {
      case Some(user) => ZIO.succeed(user.id)
      case None => ZIO.succeed(-1)
    }
  } yield result

  override def delete(fst: String): ZIO[Any, Throwable, Unit] =
    db.removeAll(fst)



/*
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

  def addBoth(fst: String, snd: String): ZIO[Any, Throwable, Boolean] = for {
    successful1 <- addFirst(fst)
    successful2 <- if(successful1) addSecond(snd) else ZIO.succeed(false)
  } yield successful1 && successful2



  def testPassword(fst: String): ZIO[Any, Throwable, Boolean] = ZIO.succeed(true)*/
}

object UserValidator {
  def layer(conf: String): ZLayer[UserDB, Throwable, UserValidator] = ZLayer {
    for {
      userdb <- ZIO.service[UserDB]
    } yield new UserValidator(userdb)
  }
}
