package DB

import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import zio.{IO, Layer, ZIO, ZLayer}

import scala.concurrent.Await
import scala.concurrent.duration.Duration




class Users(tag: Tag) extends MarkedTable[String, User](tag, "UserS") {
  def name = column[String]("NAME")
  def password = column[String]("PASSWORD")
  /*
    Permission specifications:
      100: User can manage its database                                       User
      200: User can add and remove books from general collection              Manager
      300: User can add and delete users                                      Admin
   */
  def permissionLevel: Rep[Int] = column[Int]("PERMISSIONLEVEL")
  //def banDate: Rep[Date]....
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)


  type Parameter = String
  def marked: Rep[String] = name

  def * = (name, password, id).mapTo[User]
}

case class UserDB(override val db: H2Profile.backend.JdbcDatabaseDef) extends MarkedStringDB[User, Users] {
  val tableQuery = TableQuery[Users]
}

object UserDB {
  val layer: Layer[Throwable, UserDB] = ZLayer {
    for {
      conf <- ZIO.succeed("users")
      db <- ZIO.attempt(Database.forConfig(conf))
    } yield UserDB(db)
  }
}



