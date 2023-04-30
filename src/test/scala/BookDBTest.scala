import Common.{Book, User}
import DB.{BookDB, UserDB}
import zio.test._
import zio.ZIO
import slick.jdbc.PostgresProfile.api.Database

import java.time.LocalDate


object BookDBTest extends ZIOSpecDefault {
  def spec = suite("BookDB")(
    test("addAndSearch"){
      for {
        db <- ZIO.attempt(Database.forConfig("bookdbtest"))
        bookdb <- ZIO.succeed(BookDB(db))
        _ <- bookdb.add(Book("a1","e","i"))
        search <- bookdb.generalSearch("A1")
        _ <- bookdb.removeAll("a1")
      } yield assertTrue(search == List(Book("a1","e","i")))
    },
    test("searchNonExistant") {
      for {
        db <- ZIO.attempt(Database.forConfig("bookdbtest"))
        bookdb <- ZIO.succeed(BookDB(db))
        //_ <- ZIO.fromFuture(implicit ec => bookdb.db.run(bookdb.tableQuery.))
        search <- bookdb.generalSearch("A2")
      } yield assertTrue(search == List())
    },
    test("renewLate") {
      for {
        db <- ZIO.attempt(Database.forConfig("bookdbtest"))
        bookdb <- ZIO.succeed(BookDB(db))
        _ <- bookdb.add(Book("a3", "e", "i", 0, 1, LocalDate.now().minusDays(1), 1))
        _ <- bookdb.renew(1)
        search <- bookdb.generalSearch("A3")
        _ <- bookdb.removeAll("a3")
      } yield assertTrue(search == List(Book("a3", "e", "i", 0, 1, LocalDate.now().minusDays(1), 1)))
    },
    test("renewSoon") {
      for {
        db <- ZIO.attempt(Database.forConfig("bookdbtest"))
        bookdb <- ZIO.succeed(BookDB(db))
        _ <- bookdb.add(Book("a4", "e", "i", 0, 1, LocalDate.now().plusDays(1), 1))
        _ <- bookdb.renew(1)
        search <- bookdb.generalSearch("A4")
        _ <- bookdb.removeAll("a4")
      } yield assertTrue(search.head == Book("a4", "e", "i", 0, 1, LocalDate.now().plusMonths(1), 1))
    },
    test("renewButReserved") {
      for {
        db <- ZIO.attempt(Database.forConfig("bookdbtest"))
        bookdb <- ZIO.succeed(BookDB(db))
        _ <- bookdb.add(Book("a5", "e", "i", 2, 1, LocalDate.now().plusDays(1), 1))
        _ <- bookdb.renew(1)
        search <- bookdb.generalSearch("A5")
        _ <- bookdb.removeAll("a5")
      } yield assertTrue(search.head == Book("a5", "e", "i", 2, 1, LocalDate.now().plusDays(1), 1))
    },
    test("renewButReserved") {
      for {
        db <- ZIO.attempt(Database.forConfig("bookdbtest"))
        bookdb <- ZIO.succeed(BookDB(db))
        _ <- bookdb.add(Book("a6", "e", "i", 2, 1, LocalDate.now().plusDays(1), 1))
        _ <- bookdb.renew(1)
        search <- bookdb.generalSearch("A6")
        _ <- bookdb.removeAll("a6")
      } yield assertTrue(search.head == Book("a6", "e", "i", 2, 1, LocalDate.now().plusDays(1), 1))
    },
    test("returnLate") {
      for {
        // Initialize databases
        db1 <- ZIO.attempt(Database.forConfig("bookdbtest"))
        bookdb <- ZIO.succeed(BookDB(db1))
        db2 <- ZIO.attempt(Database.forConfig("userdbtest"))
        userdb <- ZIO.succeed(UserDB(db2))

        // Add user
        _ <- userdb.add(User("name1", "pass", "mail", 0, LocalDate.MIN, 1))

        // Add book and return
        _ <- bookdb.add(Book("a7", "e", "i", 2, 1, LocalDate.now().minusDays(1), 1))
        daysLate <- bookdb.returnBook(1)
        _ <- ZIO.when(daysLate > 0) {
          userdb.ban(1, daysLate)
        }
        search <- bookdb.generalSearch("A7")
        _ <- bookdb.removeAll("a7")

        // Check user
        getUser <- userdb.search("name1")
        _ <- userdb.removeAll("name1")
      } yield assertTrue(search.head == Book("a7", "e", "i", 2, 0, LocalDate.MIN, 1)
      && getUser.head == User("name1", "pass", "mail", 0, LocalDate.now().plusDays(2), 1))
    },
    test("returnSoon") {
      for {
        // Initialize databases
        db1 <- ZIO.attempt(Database.forConfig("bookdbtest"))
        bookdb <- ZIO.succeed(BookDB(db1))
        db2 <- ZIO.attempt(Database.forConfig("userdbtest"))
        userdb <- ZIO.succeed(UserDB(db2))

        // Add user
        _ <- userdb.add(User("name2", "pass", "mail", 0, LocalDate.MIN, 1))

        // Add book and return
        _ <- bookdb.add(Book("a8", "e", "i", 2, 1, LocalDate.now().plusDays(1), 1))
        daysLate <- bookdb.returnBook(1)
        _ <- ZIO.when(daysLate > 0) {
          userdb.ban(1, daysLate)
        }
        search <- bookdb.generalSearch("A8")
        _ <- bookdb.removeAll("a8")

        // Check user
        getUser <- userdb.search("name2")
        _ <- userdb.removeAll("name2")
      } yield assertTrue(search.head == Book("a8", "e", "i", 2, 0, LocalDate.MIN, 1)
        && getUser.head == User("name2", "pass", "mail", 0, LocalDate.MIN, 1))
    }
  )

}
