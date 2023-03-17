package DB

import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import zio.{IO, ZIO}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class Ratings(tag: Tag) extends MarkedTable[Int, Rating](tag, "BOOKS") {
  def user = column[Int]("user")
  def item = column[Int]("item")
  def rating = column[Int]("rating")

  type Parameter = Int
  def marked: Rep[Int] = user

  def * = (user, item, rating).mapTo[Rating]
}

case class RatingDB(override val db: H2Profile.backend.JdbcDatabaseDef) extends MarkedIntDB[Rating, Ratings] {
  val tableQuery = TableQuery[Ratings]
}

object RatingDB {
  def apply(): IO[Throwable, RatingDB] = {
    for {
      db <- ZIO.attempt(Database.forURL("jdbc:h2:./db/ratingsdb", driver = "org.h2.Driver"))
    } yield RatingDB(db)
  }
}
