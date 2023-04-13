import Common.Book
import DB.BookDB
import zio.test._
import zio.ZIO

object DBTest extends ZIOSpecDefault {
  def spec = test("DBTest"){
    for {
      bookdb <- ZIO.service[BookDB]
      _ <- bookdb.removeAll("a")
      _ <- bookdb.add(Book("a","e","i"))
      search <- bookdb.generalSearch("A")
      _ <- bookdb.removeAll("a")
    } yield assertTrue(search == List(Book("a","e","i")))
  }.provide(BookDB.layer("bookdb"))

}
