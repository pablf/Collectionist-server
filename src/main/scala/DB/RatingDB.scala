package DB

import Common.Rating
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import zio.{Layer, ZIO, ZLayer}

class Ratings(tag: Tag) extends MarkedTable[Int, Rating](tag, "BOOKS") {
  def user: Rep[Int] = column[Int]("user")
  def item: Rep[Int] = column[Int]("item")
  def rating: Rep[Int] = column[Int]("rating")

  type Parameter = Int
  def marked: Rep[Int] = user

  def * = (user, item, rating).mapTo[Rating]
}

case class RatingDB(override val db: H2Profile.backend.JdbcDatabaseDef) extends MarkedIntDB[Rating, Ratings] {
  val tableQuery = TableQuery[Ratings]
}

object RatingDB {

  val layer: Layer[Throwable, RatingDB] = ZLayer {
    for {
      db <- ZIO.attempt(Database.forURL("jdbc:h2:./db/ratingsdb", driver = "org.h2.Driver"))
    } yield RatingDB(db)
  }

}
