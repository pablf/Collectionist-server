package DB

import App.Book
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class Rating(val user: Int, val item: Int, val rating: Int) extends Serializable

class Ratings(tag: Tag) extends MarkedTable[Int, Rating](tag, "BOOKS") {
  def user = column[Int]("user")
  def item = column[Int]("item")
  def rating = column[Int]("rating")

  type Parameter = Int
  def marked: Rep[Int] = user

  def * = (user, item, rating).mapTo[Rating]
}

case class RatingDB(tag: String) extends MarkedDB[Int, Rating, Ratings]{
  val tableQuery = TableQuery[Ratings]
  val conf = "ratings"
  val db = Database.forURL("jdbc:h2:./db/ratingsdb", driver = "org.h2.Driver")
  db.run(tableQuery.schema.create)


/*
  def search(searchTerm: Int): List[Rating] = {
    val q = tableQuery.filter(_.user === searchTerm).result
    val s = db.run(q)
    val r = Await.result(s,Duration.Inf).toList
    r
  }*/

  def add(book: Rating): Unit = db.run(tableQuery += book)
  //def removeAll(book: Rating): Unit = db.run(tableQuery.filter(_.name =!= book.name).result)

}

