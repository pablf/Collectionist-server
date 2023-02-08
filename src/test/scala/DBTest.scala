import App.Book
import DB.BookDB
import zio.test._

object DBTest extends ZIOSpecDefault {
  def spec = test("DBTest"){
    val db = BookDB("a", "bookdb")
    db.add(Book("a","e","i"))
    assertTrue(db.search("a")==List(Book("a","e","i")))
  }

}
