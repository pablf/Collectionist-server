package Recommendation

import App.{Book, Profile}
import DB.BookDB
import org.apache.spark._

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import java.util.Properties
//import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

import scala.collection.mutable.ListBuffer

class Recommender(bookdb: BookDB) {
  val NRecommendations: Int = 50

  val sc = new SparkConf().setMaster("local[1]")
  val spark = SparkSession.builder().appName("Recommender").config(sc).getOrCreate()

  val connectionProperties: Properties = new Properties()
  //connectionProperties.put("user", "username")
  //connectionProperties.put("password", "password")
  connectionProperties.put("driver", "org.h2.Driver")


  val df: DataFrame = spark.read.jdbc("jdbc:h2:./db/ratingsdb", "books", connectionProperties)
  val model: ALSModel = new ALS().fit(prepare(df))

  //def similarBooks():
  case class User(val user: Int)
  import spark.implicits._
  def giveRecommendation(user: Int): List[Book] = {
    val rec = model.recommendForUserSubset(Seq(User(user)).toDS, 50)
    rec.collect().flatMap(row => bookdb.find(row.getInt(1))).toList  // why toList???
  }

  //TODO
  def prepare(df: DataFrame): DataFrame = df



}
