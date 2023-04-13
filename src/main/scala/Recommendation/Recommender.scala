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

class Recommender(bookdb: BookDB) {
  val NRecommendations: Int = 50


  val sconf: SparkConf = new SparkConf()
    .setMaster("local[1]")
    .setAppName("Recommender")
  val sc: SparkContext = new SparkContext(sconf)
  val spark: SparkSession = SparkSession.builder().config(sc.getConf).getOrCreate()



  val connectionProperties: Properties = new Properties()
  connectionProperties.put("driver", "org.h2.Driver")
  //connectionProperties.put("user", "username")
  //connectionProperties.put("password", "password")
  val df: DataFrame = spark.read.jdbc("jdbc:h2:./db/ratingsdb", "books", connectionProperties)
  val model: ALSModel = new ALS().fit(prepare(df))


  case class User(user: Int)
  import spark.implicits._
  def giveRecommendation(user: Int): IO[Throwable, Array[Book]] = for{
    //df.filter(row => row.getInt(0) == user).isEmpty
    rec <-
      if(true) ZIO.succeed(model.recommendForAllUsers(1))
      else ZIO.succeed(model.recommendForUserSubset(Seq(User(user)).toDS, 1))   //scala.math.min(50, df.collect().length)
    //_ <- printLine("2 " + parser(rec).toList)
    books <- bookdb.getBooks(List(0))
    //books <- bookdb.getBooks(rec.collect().map(_.getInt(1)).toList)
    //books <- ZIO.foreach(rec.collect().map(_.getInt(1)))(row => bookdb.find(row)).map(_.flatten)
  } yield books.toArray // why toList???


  def prepare(df: DataFrame): DataFrame = df

  /*
   *  Class model.recommendForUserSubset gives a DataFrame with schema ("user", "recommendation")
   *  where the items in recommendation are ArraySeq[GenericRowWithSchema] with schema ("item", "rating")
   *  parser(df) gives an Array extracting the int in "item"
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