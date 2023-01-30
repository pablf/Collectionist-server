package DB

import App.Book
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class Books(tag: Tag) extends Table[Book](tag, "BOOKS") {
  def name = column[String]("NAME")
  def author = column[String]("AUTHOR")
  def genre = column[String]("GENRE")

  def * = (name, author, genre).mapTo[Book]
}

case class BookDB(tag: String) {//extends DB[Book] {
  type TType = Books
  val tableQuery = TableQuery[Books]
  val conf = "books"
  val db = Database.forConfig("books")
  db.run(tableQuery.schema.create)

  def path: String = ???

  def search(searchTerm: String): List[Book] = {
    val q = tableQuery.filter(_.name === searchTerm).result
    val s = db.run(q)
    val r = Await.result(s,Duration.Inf).toList
    r
  }

  def add(book: Book): Unit = db.run(tableQuery += book)
  def removeAll(book: Book): Unit = db.run(tableQuery.filter(_.name =!= book.name).result)

}




