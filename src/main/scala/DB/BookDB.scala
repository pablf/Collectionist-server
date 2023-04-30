package DB

import Common.Book
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import zio.{IO, Layer, Task, ZIO, ZLayer, durationInt}

import scala.math.max

import java.time.LocalDate

/*
 *  Database to save all the books in Collectionist. It also save the status on the
 *  library: if it is borrowed or reserved. If it is not, the reservedId or borrowedId must be 0,
 *  that does not represent any user.
 *  If reservedId != 0, date must the date when the reserve of the book finish, that is,
 *  the user reservedId has the right to borrow the book before the date.
 *  If borrowedId != 0, date must the date when the book must be returned and when returned,
 *  there is a penalization if the book has not been returned in time.
 */

class Books(tag: Tag) extends MarkedTable[String, Book](tag, "BOOKS") {
  def name: Rep[String] = column[String]("NAME")
  def author: Rep[String] = column[String]("AUTHOR")
  def genre: Rep[String] = column[String]("GENRE")
  def reservedId: Rep[Int] = column[Int]("RESERVEDID")
  def borrowedId: Rep[Int] = column[Int]("TAKENID")
  def date: Rep[LocalDate] = column[LocalDate]("DATE")

  type Parameter = String
  def marked: Rep[String] = name

  def * = (name, author, genre, reservedId, borrowedId, date, id) <> (Book.tupled, Book.unapply _)
}

case class BookDB(override val db: PostgresProfile.backend.JdbcDatabaseDef) extends MarkedStringDB[Book, Books] {
  val tableQuery = TableQuery[Books]

  def possibleTags: Array[String] = Array("Name", "Author", "Genre", "Id")

  def generalSearch(item: String): IO[Throwable, List[Book]] = {
    val query = tableQuery.filter(book => (book.name.toLowerCase like s"%${item.toLowerCase()}%")
      || (book.author.toLowerCase like s"%${item.toLowerCase()}%")
      || (book.genre.toLowerCase like s"%${item.toLowerCase()}%")).result
    ZIO.fromFuture(implicit ec => db.run(query)).map(_.toList)
  }

  def advancedSearch(items: List[String], tags: List[String]): IO[Throwable, List[Book]] = {
    if (items.length != tags.length) ZIO.fail(new Throwable())
    else {
      val query = composeQuery(items, tags).result
      ZIO.fromFuture(implicit ec => db.run(query)).map(_.toList)
    }
  }

  def reserve(userId: Int, bookId: Int): Task[Int] = {
    val query = tableQuery.filter(_.id === bookId).filter(_.reservedId =!= 0).map(_.reservedId).update(userId)
    ZIO.fromFuture(implicit ec => db.run(query))
  }

  def renew(bookId: Int): Task[Int] = {
    val query =
      tableQuery.filter(_.id === bookId).filter(_.reservedId === 0).filter(_.date >= LocalDate.now())
        .map(_.date).update(LocalDate.now().plusMonths(1))
    ZIO.fromFuture(implicit ec => db.run(query))
  }

  def returnBook(bookId: Int): Task[Int] = {
    val queryId = tableQuery.filter(_.id === bookId).map(_.borrowedId).update(0)
    val queryDate = tableQuery.filter(_.id === bookId).map(_.date).update(LocalDate.MIN)
    val queryInTime = tableQuery.filter(_.id === bookId).map(_.date).result //checks if it was returned in time
    val now = LocalDate.now()
    for {
      _ <- ZIO.fromFuture(implicit ec => db.run(queryId))
      date <- ZIO.fromFuture(implicit ec => db.run(queryInTime))
      _ <- ZIO.fromFuture(implicit ec => db.run(queryDate))
    } yield date.headOption match {
      case None => 0
      case Some(d) => max(0, 365*(now.getYear - d.getYear) + now.getDayOfYear - d.getDayOfYear)
    }

  }


  // Methods for advancedSearch
  // Composes the queries given by determineQuery(items(i), tags(i))
  def composeQuery(items: List[String], tags: List[String]): Query[Books, Books#TableElementType, Seq] = {
    if (items.isEmpty && tags.isEmpty) {
      val query = determineQuery(items.head, tags.head)
      query ++ composeQuery(items.tail, tags.tail)
    } else tableQuery.filterIf(false)(_.name === "")
  }

  // Gives query to search item in parameter tag
  def determineQuery(item: String, tag: String): Query[Books, Books#TableElementType, Seq] = tag match {
    case "Name" => tableQuery.filter(_.name === item)
    case "Author" => tableQuery.filter(_.author === item)
    case "Genre" => tableQuery.filter(_.genre === item)
    case "Id" => item match {
      case Int(x) => tableQuery.filter(_.id === x)
      case _ => tableQuery.filterIf(false)(_.name === item) // query that returns nothing???
    }
    case _ => tableQuery.filterIf(false)(_.name === item)
  }

  // Unapply for match
  object Int {
    def unapply(s: String): Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }

}

object BookDB {

  val layer: Layer[Throwable, BookDB] = ZLayer {
    for {
      db <- ZIO.attempt(Database.forConfig("bookdb"))
    } yield BookDB(db)
  }

}