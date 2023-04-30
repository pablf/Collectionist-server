package DB

import Common.Rating
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import zio.{Layer, ZIO, ZLayer}

/*
 * Database read by the Recommender service that saves the ratings of the books by the users.
 */

class Ratings(tag: Tag) extends MarkedTable[Int, Rating](tag, "BOOKS") {
  def user: Rep[Int] = column[Int]("user")
  def item: Rep[Int] = column[Int]("item")
  def rating: Rep[Int] = column[Int]("rating")

  type Parameter = Int
  def marked: Rep[Int] = user

  def * = (user, item, rating).mapTo[Rating]
}

case class RatingDB(override val db: PostgresProfile.backend.JdbcDatabaseDef) extends MarkedIntDB[Rating, Ratings] {
  val tableQuery = TableQuery[Ratings]
}

object RatingDB {

  val layer: Layer[Throwable, RatingDB] = ZLayer {
    for {
      db <- ZIO.attempt(Database.forConfig("ratingdb"))
    } yield RatingDB(db)
  }

}
