package Recommendation

import App.{Book, Profile}
import org.apache.spark._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
//import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.rdd.RDD
import org.apache.log4j.{Level, Logger}

import scala.collection.mutable.ListBuffer

class Recommender(user: Profile){
  val NRecommendations: Int = 50

  val sc = new SparkConf()
  val spark = SparkSession.builder().appName("Recommender").config(sc).getOrCreate()

  val df: DataFrame = spark.read.jdbc(user.likedBooks.path)


  val model: ALSModel = new ALS().fit(prepare(df))
  val recommendations: List[Book] = giveRecommendation()

  def similarBooks():

  def giveRecommendation(): Book = {
    val rec = model.recommendForAllUsers(50)

  }

  def prepare(df: DataFrame): DataFrame = ???



}
