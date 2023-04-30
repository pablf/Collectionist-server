package Recommendation


import Common.Book
import DB.BookDB
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import zio.{IO, ZIO, ZLayer}

import scala.collection.mutable.ArraySeq
import java.util.Properties

/*
 * Recommender of books using spark.ml that reads from a RatingDB
 */

class Recommender(bookdb: BookDB) {
  // Number of recommendations that ask the algorithm
  val NRecommendations: Int = 50


  // Create SparkSession.
  val sconf: SparkConf = new SparkConf()
    .setMaster("local[1]")
    .setAppName("Recommender")
  val sc: SparkContext = new SparkContext(sconf)
  val spark: SparkSession = SparkSession.builder().config(sc.getConf).getOrCreate()


  // Read RatingDB and construct ALSModel.
  val connectionProperties: Properties = new Properties()
  connectionProperties.put("driver", "org.postgres.Driver")
  val df: DataFrame = spark.read.jdbc("jdbc:postgres:./db/ratingsdb", "books", connectionProperties)
  val model: ALSModel = new ALS().fit(prepare(df))


  case class User(user: Int)
  import spark.implicits._
  def giveRecommendation(user: Int): IO[Throwable, Array[Book]] = for{
    rec <-
      if(true) ZIO.succeed(model.recommendForAllUsers(NRecommendations))
      else ZIO.succeed(model.recommendForUserSubset(Seq(User(user)).toDS, NRecommendations))
    books <- bookdb.findList(rec.collect().map(_.getInt(1)).toList)
  } yield books.toArray


  def prepare(df: DataFrame): DataFrame = df

  /*
   *  Method model.recommendForUserSubset gives a DataFrame with schema ("user", "recommendation")
   *  where the items in recommendation are ArraySeq[GenericRowWithSchema] with schema ("item", "rating").
   *  parser(df) gives an Array extracting the int in "item".
   */
  def parser(df: DataFrame): Array[Int] =
    df.collect()
      .map(_.get(1))
      .map {
        case a: ArraySeq[_] => a.head match {
          case b: GenericRowWithSchema => b.getInt(0)
          case _ => -1
        }
        case _ => -1
      }


}

object Recommender {

  val layer: ZLayer[BookDB, Throwable, Recommender] = ZLayer {
    for {
      bookdb <- ZIO.service[BookDB]
    } yield new Recommender(bookdb)
  }

}