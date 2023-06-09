import DB.BookDB
import zio.test._
import zio.ZIO
import slick.jdbc.PostgresProfile.api.Database

import Recommendation.Recommender

object RecommenderTest extends ZIOSpecDefault {
  def spec = test("RecommenderTest"){
    for {
      bookdb <- ZIO.attempt(BookDB(Database.forConfig("bookdbtest")))
      rec <- new Recommender(bookdb).giveRecommendation(0)
    } yield assertTrue(rec.nonEmpty)
  }

}
