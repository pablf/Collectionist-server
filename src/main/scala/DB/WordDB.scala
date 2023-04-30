package DB

import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Filters._
import zio.{Task, UIO, ZIO}

/*
 *  A reader of MongoDB that records all the words appearing in the BookDB.
 */

class WordDB() extends scala.AnyRef {

  case class Word(word: String, prob: Int)

  val client : MongoClient = MongoClient("mongodb://localhost:27017/")
  val db : MongoDatabase = client.getDatabase("mydb")
  val collection: MongoCollection[Word] = db.getCollection("words")

  /*
   *  Returns the words of list that are in the db.
   */
  def coincidences(list : List[String]) : UIO[List[String]] =
    list.foldLeft(ZIO succeed List[String]()){
      case (searched, word) => for {
        coincidence <-
          ZIO.fromFuture(implicit ec => collection.find(equal("word", word)).toFuture.map(_.toList))
            .catchAll(_ => ZIO succeed List[Word]())
        words <- searched
      } yield coincidence.map(_.word) ++ words
    }

  // Returns probability of word from 0 to 1000.
  def prob(word: String): Task[Int] =
    ZIO.fromFuture(implicit ec => collection.find(equal("word", word)).first().headOption())
      .map {
        case None => 0
        case Some(Word(_, probability)) => probability
      }

}
