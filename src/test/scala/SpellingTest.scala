import zio.test._
import Spelling.SpellingChecker
import zio.ZIO

object SpellingTest extends ZIOSpecDefault {
  def spec = suite("SpellingTest")(
    test("join"){
      val list: List[ZIO[Any, Throwable, String]] = List(ZIO.succeed("a"), ZIO.succeed("e"), ZIO.succeed("i"))
      assertZIO(SpellingChecker.join(list))(Assertion.equalTo("a e i"))
    },
    test("parse") {
      assertTrue(SpellingChecker.parse("a e o") == List("a", "e", "o"))
      assertTrue(SpellingChecker.parse(" e  o ") == List("e", "o"))
      assertTrue(SpellingChecker.parse("") == List())
      assertTrue(SpellingChecker.parse("    ") == List())
    },
    test("getWord") {
      assertTrue(SpellingChecker.getWord("a e o") == ("a", "e o"))
      assertTrue(SpellingChecker.getWord(" e  o ") == ("e", " o "))
      assertTrue(SpellingChecker.getWord("") == ("", ""))
      assertTrue(SpellingChecker.getWord("    ") == ("", "   "))
    },
    test("splitEverywhere") {
      assertTrue(SpellingChecker.splitEverywhere("aeo") == List(("", "aeo"),("a", "eo"), ("ae","o"),("aeo","") ))
      assertTrue(SpellingChecker.splitEverywhere("") == List(("", "")))
    }
  )
}
