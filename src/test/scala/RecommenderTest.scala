import DB.{Book, BookDB}
import zio.test._

import Recommendation.Recommender

object RecommenderTest extends ZIOSpecDefault {
  def spec = test("RecommenderTest"){
    for {
      bookdb <- BookDB("a", "bookdb")
      rec <- new Recommender(bookdb).giveRecommendation(0)
    } yield assertTrue(rec.nonEmpty)
  }

}
