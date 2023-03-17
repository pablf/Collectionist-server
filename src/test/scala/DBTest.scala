import DB.Book
import DB.BookDB
import zio.test._

object DBTest extends ZIOSpecDefault {
  def spec = test("DBTest"){
    for {
      bookdb <- BookDB("a", "bookdb")
      _ <- bookdb.removeAll("a")
      _ <- bookdb.add(Book("a","e","i"))
      search <- bookdb.generalSearch("A")
    } yield assertTrue(search==List(Book("a","e","i")))
  }

}
