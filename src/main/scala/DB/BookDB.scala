package DB

import App.Book
import slick.jdbc.H2Profile.api._
import zio.ZIO

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class Books(tag: Tag) extends MarkedTable[String, Book](tag, "BOOKS") {
  def name = column[String]("NAME")
  def author = column[String]("AUTHOR")
  def genre = column[String]("GENRE")
  def id = column[Int]("ID")

  type Parameter = String
  def marked: Rep[String] = name



  def * = (name, author, genre, id).mapTo[Book]
}

case class BookDB(tag: String, path: String) extends MarkedDB[String, Book, Books] {
  val tableQuery = TableQuery[Books]
  val db = ZIO.attempt(Database.forURL("jdbc:h2:./db/" ++ path, driver = "org.h2.Driver"))
  /*
  val db = Database.forURL("jdbc:h2:./db/" ++ path, driver = "org.h2.Driver")
  db.run(tableQuery.schema.create)


  def search(searchTerm: String): List[Book] = {
    val q = tableQuery.filter(_.name === searchTerm).result
    val s = db.run(q)
    val r = Await.result(s,Duration.Inf).toList
    r
  }

  def find(id: Int): List[Book] = {
    val q = tableQuery.filter(_.id === id).result
    val s = db.run(q)
    val r = Await.result(s, Duration.Inf).toList
    r
  }

  def add(book: Book): Unit = db.run(tableQuery += book)
  def removeAll(book: Book): Unit = db.run(tableQuery.filter(_.name =!= book.name).result)
*/
}




