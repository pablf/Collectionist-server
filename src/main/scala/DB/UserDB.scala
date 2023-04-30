package DB

import Common.User
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import zio.{Layer, Task, ZIO, ZLayer}

import java.time.LocalDate

/*
 * Database with all the users of Collectionist.
 */

class Users(tag: Tag) extends MarkedTable[String, User](tag, "Users") {
  def name = column[String]("NAME")
  def password = column[String]("PASSWORD")
  def mail = column[String]("MAIL")
  /*
    Permission specifications:
        0: Banned user
      100: User can manage its database                                       User
      200: User can add and remove books from general collection              Manager
      300: User can add and delete users                                      Admin
   */
  def permissionLevel: Rep[Int] = column[Int]("PERMISSIONLEVEL")
  def banDate: Rep[LocalDate] = column[LocalDate]("BANDATE")

  type Parameter = String
  def marked: Rep[String] = name

  def * = (name, password, mail, permissionLevel, banDate, id) <> (User.tupled, User.unapply _)
}

case class UserDB(override val db: PostgresProfile.backend.JdbcDatabaseDef) extends MarkedStringDB[User, Users] {
  val DaysBannedPerDayLate: Int = 2

  val tableQuery = TableQuery[Users]

  // Slick doesn't allow to apply functions when updating
  def ban(userId: Int, daysLate: Int): Task[Unit] = {
    val queryGet = tableQuery.filter(_.id === userId).map(_.banDate).result
    def querySet(newDate: LocalDate) = tableQuery.filter(_.id === userId).map(_.banDate).update(newDate)
    for {
      date <- ZIO.fromFuture(implicit ec => db.run(queryGet))
      _ <- ZIO.when(date.nonEmpty) {
        ZIO.fromFuture(implicit ec => db.run(querySet(date.head.plusDays(DaysBannedPerDayLate * daysLate))))
      }
    } yield ()
  }
  
}

object UserDB {

  val layer: Layer[Throwable, UserDB] = ZLayer {
    for {
      db <- ZIO.attempt(Database.forConfig("userdb"))
    } yield UserDB(db)
  }

}