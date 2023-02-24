package DB

import slick.jdbc.H2Profile.api._
import zio.ZIO

import scala.concurrent.Await
import scala.concurrent.duration.Duration


case class User(val name: String, val password: String, val id: Int) extends Serializable

class Users(tag: Tag) extends MarkedTable[String, User](tag, "UserS") {
  def name = column[String]("NAME")
  def password = column[String]("PASSWORD")
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  type Parameter = String
  def marked: Rep[String] = name

  def * = (name, password, id).mapTo[User]
}

case class UserDB(tag: String) extends MarkedDB[String, User, Users] {
  val tableQuery = TableQuery[Users]
  val conf = "users"
  val db = ZIO.attempt(Database.forConfig(conf))


  /*
  def search(searchTerm: String): List[User] = {
    val q = tableQuery.filter(_.name === searchTerm).result
    val s = db.run(q)
    val r = Await.result(s,Duration.Inf).toList
    r
  }

  def add(user: User): Unit = db.run(tableQuery += user)
  def removeAll(User: User): Unit = db.run(tableQuery.filter(_.name =!= User.name).result)
  def update(user: User): Unit = db.run(tableQuery.update(user))

*/
}




