package DB

import Common.Book
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import zio.{IO, Layer, ZIO, ZLayer}
import zio.durationInt

class Books(tag: Tag) extends MarkedTable[String, Book](tag, "BOOKS") {
  def name: Rep[String] = column[String]("NAME")
  def author: Rep[String] = column[String]("AUTHOR")
  def genre: Rep[String] = column[String]("GENRE")
  def id: Rep[Int] = column[Int]("ID")

  type Parameter = String
  def marked: Rep[String] = name

  def * = (name, author, genre, id).mapTo[Book]
}

case class BookDB(override val db: H2Profile.backend.JdbcDatabaseDef) extends MarkedStringDB[Book, Books] {
  val tableQuery = TableQuery[Books]

  def possibleTags: Array[String] = Array("Name", "Author", "Genre", "Id")

  // similar to method search but with ID
  def find(id: Int): IO[Throwable, List[Book]] = for {
    query <- ZIO.succeed(tableQuery.filter(_.id === id).result)
    result <- ZIO.fromFuture(implicit ec => db.run(query)).timeoutFail(new Throwable)(5.second)
  } yield result.toList

  def getBooks(rec: List[Int]): IO[Throwable, List[Book]] = ZIO
      .collectAll(rec.map(n => find(n)))
      .map(_.flatten)

  def generalSearch(item: String): IO[Throwable, List[Book]] = {
    val query = tableQuery.filter(book => (book.name.toLowerCase like s"%${item.toLowerCase()}%")
      || (book.author.toLowerCase like s"%${item.toLowerCase()}%")
      || (book.genre.toLowerCase like s"%${item.toLowerCase()}%")).result
    ZIO.fromFuture(implicit ec => db.run(query)).map(_.toList)
  }

  def advancedSearch(items: List[String], tags: List[String]): IO[Throwable, List[Book]] = {
    if(items.length != tags.length) ZIO.fail(new Throwable())
    else {
      val query = composeQuery(items, tags).result
      ZIO.fromFuture(implicit ec => db.run(query)).map(_.toList)
    }
  }

  def composeQuery(items: List[String], tags: List[String]): Query[Books, Books#TableElementType, Seq] = {
    if(items.isEmpty && tags.isEmpty) {
      val query = determineQuery(items.head, tags.head)
      query ++ composeQuery(items.tail, tags.tail)
    } else tableQuery.filterIf(false)(_.name === "")
  }

  def determineQuery(item: String, tag: String): Query[Books, Books#TableElementType, Seq] = tag match {
    case "Name" => tableQuery.filter(_.name === item)
    case "Author" => tableQuery.filter(_.author === item)
    case "Genre" => tableQuery.filter(_.genre === item)
    case "Id" => item match {
      case Int(x) => tableQuery.filter(_.id === x)
      case _ => tableQuery.filterIf(false)(_.name === item)// query that returns nothing???
    }
    case _ => tableQuery.filterIf(false)(_.name === item)
  }



  def searchTag(item: String, tag: String): IO[Throwable, List[Book]] = {
    val query = determineQuery(item, tag).result
    ZIO.fromFuture(implicit ec => db.run(query)).map(_.toList)
  }

  object Int {
    def unapply(s: String): Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _: java.lang.NumberFormatException => None
    }
  }

}



object BookDB {

  def layer(path: String): Layer[Throwable, BookDB] = ZLayer {
    for {
      db <- ZIO.attempt(Database.forURL("jdbc:h2:./db/" ++ path, driver = "org.h2.Driver"))
    } yield BookDB(db)
  }

}




