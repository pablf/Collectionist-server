package App

import DB.BookDB

import scala.collection.mutable.Map
import org.apache.spark.sql.Row

case class Book(name: String = "na", author: String = "na", genre: String = "na", id: Int = 0) extends Serializable {

  val getFields: List[String] = List("name", "author", "genre")
}

object Book {

  def make(row: Row): Book = Book(row.getString(0), row.getString(1), row.getString(2), row.getInt(3))

  def make(map: Map[String, String]): Book = Book(map("name"), map("author"), map("genre"))
  //final case class NoBook() extends Book("na","na","na")
  //final case class SampleBook() extends Book
}