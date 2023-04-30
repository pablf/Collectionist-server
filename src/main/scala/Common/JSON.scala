package Common

import zio.http.Response
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}
import zio.{IO, ZIO}

import java.time.LocalDate

/*
 * Object with methods for JSON encoding and decoding. It processes Books, Booleans and Strings.
 */

object JSON {

  // Encoding methods

  implicit val bookEncoder: JsonEncoder[Book] = DeriveJsonEncoder.gen[Book]

  def encodeBook(book: Book): CharSequence = bookEncoder.encodeJson(book, None)

  def encodeBooks(books: Array[Book]): CharSequence = books.toJson

  def encodeBooks(books: List[Book]): CharSequence = books.toJson

  def encodeStrings(strings: Array[String]): CharSequence = strings.toJson

  // Decoding methods

  implicit val bookDecoder: JsonDecoder[Book] = DeriveJsonDecoder.gen[Book]

  def decodeBook(book: String): Book =
    bookDecoder.decodeJson(book) match {
      case Left(_) => Book()
      case Right(b) => b
    }

  def decodeBooks(books: String): Array[Book] =
    books.fromJson[Array[Book]] match {
      case Left(_) => Array[Book]()
      case Right(arr) => arr
    }

  def decodeBoolean(response: Response): IO[Throwable, Boolean] =
    response.body.asString.flatMap(text => text.toBooleanOption match {
      case None => ZIO.fail(new Throwable)
      case Some(b) => ZIO.succeed(b)
    })

  def decodeStrings(strings: String): Array[String] =
    JsonDecoder[Array[String]].decodeJson(strings) match {
      case Left(_) => Array()
      case Right(arr) => arr
    }

}
