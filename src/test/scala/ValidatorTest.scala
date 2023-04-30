import DB.UserDB
import Recommendation.Recommender
import Validator.UserValidator
import zio.{Layer, ZIO, ZLayer}
import zio.test._
import slick.jdbc.PostgresProfile.api.Database

object ValidatorTest extends ZIOSpecDefault {
  def spec = test("ValidatorTest"){
    val testLayer: Layer[Throwable, UserDB] = ZLayer {
      for {
        db <- ZIO.attempt(Database.forConfig("userdbtest"))
      } yield UserDB(db)
    }

    {
      for {
        validator <- ZIO.service[UserValidator]
        _ <- validator.add("Fake", "Fake")
        didLogin <- validator.tryLogin("Fake", "Fake")
        _ <- validator.delete("Fake")
      } yield assertTrue(didLogin)
    }.provide(UserValidator.layer, testLayer)
  }

}
