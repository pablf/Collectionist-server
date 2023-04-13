package Common

import org.apache.spark.sql.Row

import scala.collection.mutable.Map

case class Book(
                 name: String = "na",
                 author: String = "na",
                 genre: String = "na",
                 id: Int = 0
               ) extends Serializable {

  val getFields: List[String] = List("name", "author", "genre")

}

object Book {

  def make(row: Row): Book = Book(row.getString(0), row.getString(1), row.getString(2), row.getInt(3))

  def make(map: Map[String, String]): Book = Book(map("name"), map("author"), map("genre"))

}