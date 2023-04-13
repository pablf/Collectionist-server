import DB.UserDB
import Recommendation.Recommender
import Validator.UserValidator
import zio.ZIO
import zio.test._

object ValidatorTest extends ZIOSpecDefault {
  def spec = test("ValidatorTest"){
    for {
      validator <- ZIO.service[UserValidator]
      _ <- validator.add("Fake", "Fake")
      didLogin <- validator.tryLogin("Fake", "Fake")
      _ <- validator.delete("Fake")
    } yield assertTrue(didLogin)
  }.provide(UserValidator.layer("user"), UserDB.layer)

}
