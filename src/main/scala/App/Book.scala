package App

import scala.collection.mutable.Map

case class Book(name: String = "na", author: String = "na", genre: String = "na") extends Serializable {

  val getFields: List[String] = List("name", "author", "genre")
}

object Book {

  def make(map: Map[String, String]): Book = Book(map("name"), map("author"), map("genre"))
  //final case class NoBook() extends Book("na","na","na")
  //final case class SampleBook() extends Book
}