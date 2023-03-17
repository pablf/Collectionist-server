package Recommendation

import App.Profile
import DB.{Book, BookDB}
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark._
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import zio.Console.printLine
import zio.{IO, Task, UIO, ZIO}

import scala.collection.mutable.ArraySeq

import java.util.Properties
import scala.::
//import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

import scala.collection.mutable.ListBuffer

import org.apache.log4j.{Level, Logger}

class Recommender(bookdb: BookDB) {
  val NRecommendations: Int = 50



  val sconf = new SparkConf().setMaster("local[1]").setAppName("Recommender")
  val sc = new SparkContext(sconf)
  Logger.getRootLogger().setLevel(Level.OFF)

  Logger.getLogger("org.apache.spark").setLevel(Level.OFF)
  Logger.getLogger("org.spark-project").setLevel(Level.OFF)
  val spark = SparkSession.builder().config(sc.getConf).getOrCreate()

  val connectionProperties: Properties = new Properties()
  //connectionProperties.put("user", "username")
  //connectionProperties.put("password", "password")
  connectionProperties.put("driver", "org.h2.Driver")


  val df: DataFrame = spark.read.jdbc("jdbc:h2:./db/ratingsdb", "books", connectionProperties)
  val model: ALSModel = new ALS().fit(prepare(df))

  //def similarBooks():
  case class User(val user: Int)
  import spark.implicits._
  def giveRecommendation(user: Int): IO[Throwable, Array[Book]] = for{
    //df.filter(row => row.getInt(0) == user).isEmpty
    rec <- if(true) printLine("1.5") *> ZIO.succeed( model.recommendForAllUsers(1))
    else ZIO.succeed( model.recommendForUserSubset(Seq(User(user)).toDS, 1))   //scala.math.min(50, df.collect().length)
    _ <- printLine(parser(rec).toList)
    books <- bookdb.getBooks(parser(rec).toList)
    _ <- printLine(books.toList)
    //books <- bookdb.getBooks(rec.collect().map(_.getInt(1)).toList)
    //books <- ZIO.foreach(rec.collect().map(_.getInt(1)))(row => bookdb.find(row)).map(_.flatten)
  } yield books.toArray // why toList???



  //TODO
  def prepare(df: DataFrame): DataFrame = df

  /*model.recommendForUserSubset gives a DataFrame with schema ("user", "recommendation")
    where the items in recommendation are ArraySeq[GenericRowWithSchema] with schema ("item", "rating")
    parser(df) gives an Array extracting the int in "item"
   */
  def parser(df: DataFrame): Array[Int] = df.collect().map(_.get(1)).map(row => row match {
    case a: ArraySeq[_] => a.head match {
      case b: GenericRowWithSchema => b.getInt(0)
      case _ => -1
    }
    case _ => -1
  })


}
