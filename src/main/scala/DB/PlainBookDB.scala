package DB

import Common.PlainBook
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.PostgresProfile.backend.Database
import slick.lifted.ProvenShape
import zio.{ZIO, IO}

/*
 * Database for PlainBook to save the list of books done by users.
 */

class PlainBooks(tag: Tag) extends MarkedTable[String, PlainBook](tag, "PLAINBOOKS") {
  def name: Rep[String] = column[String]("NAME")
  def author: Rep[String] = column[String]("AUTHOR")
  def genre: Rep[String] = column[String]("GENRE")

  type Parameter = String

  def marked: Rep[String] = column[String]("NAME")

  def * : ProvenShape[PlainBook] = (name, author, genre, id) <> (PlainBook.tupled, PlainBook.unapply _)
}

case class PlainBookDB(override val db: Database) extends MarkedStringDB[PlainBook, PlainBooks] with Serializable {
  val tableQuery: TableQuery[PlainBooks] = TableQuery[PlainBooks]
  def possibleTags: Array[String] = Array("Name", "Author", "Genre", "Id")
}
object PlainBookDB extends Serializable {
  def apply(path: String): IO[Throwable, PlainBookDB] = for {
      db <- ZIO.attempt(Database.forURL("jdbc:postgres:./db/" ++ path, driver = "org.postgres.Driver"))
    } yield PlainBookDB(db)
}
