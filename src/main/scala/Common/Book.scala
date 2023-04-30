package Common

import org.apache.spark.sql.Row

import scala.collection.mutable.Map
import java.time.LocalDate

/*
 * Template class for the objects of BookDB. It represents a book and register the state in the library: if it is borrowed
 * or reserved and the date in which reservation or borrowing ends. If it is borrowed, date is the date of the end of the
 * borrowing. If it is also reserved, the reservation period start after returning, so it is not possible to register the
 * date in which reservation ends while it is borrowed. reservedId and borrowedId must be 0 if it is not reserved or borrowed
 * and the id of the corresponding user in other case.
 */

case class Book(
                 name: String = "na",
                 author: String = "na",
                 genre: String = "na",
                 reservedId: Int = 0,
                 borrowedId: Int = 0,
                 date: LocalDate = LocalDate.MIN,
                 override val id: Int = 0
               ) extends WithId {

  val getFields: List[String] = List("name", "author", "genre")

  def update(newDate: LocalDate): Book = Book(name, author, genre, reservedId, borrowedId, newDate, id)

  def toPlainBook: PlainBook = PlainBook(name, author, genre, id)

}
