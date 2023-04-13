import DB.BookDB
import zio.test._
import zio.ZIO

import Recommendation.Recommender

object RecommenderTest extends ZIOSpecDefault {
  def spec = test("RecommenderTest"){
    for {
      bookdb <- ZIO.service[BookDB]
      rec <- new Recommender(bookdb).giveRecommendation(0)
    } yield assertTrue(rec.nonEmpty)
  }.provide(BookDB.layer("bookdb"))

}
