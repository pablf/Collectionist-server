package Recommendation

import App.{Book, Profile}
import org.apache.spark._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import java.util.Properties
//import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

import scala.collection.mutable.ListBuffer

class Recommender(user: Profile){
  val NRecommendations: Int = 50

  val sc = new SparkConf().setMaster("local")
  val spark = SparkSession.builder().appName("Recommender").config(sc).getOrCreate()

  val connectionProperties: Properties = new Properties()
  connectionProperties.put("user", "username")
  connectionProperties.put("password", "password")
  val df: DataFrame = spark.read.jdbc(user.likedBooks.path, "books", connectionProperties)


  val model: ALSModel = new ALS().fit(prepare(df))
  val recommendations: List[Book] = giveRecommendation()

  //def similarBooks():

  def giveRecommendation(): List[Book] = {
    val rec = model.recommendForAllUsers(50)
    rec.collect().map(Book.make).toList
  }


  def prepare(df: DataFrame): DataFrame = ???



}
