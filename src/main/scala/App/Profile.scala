package App

import DB.{BookDB, User, UserDB}

class Profile(val name: String) {
  // DB of books that make profile of users
  val readBooks: BookDB
  val toReadBooks: BookDB


  val favouriteBooks: BookDB
  val likedBooks: BookDB
  val dislikedBooks: BookDB
  val collectionsBooks: List[BookDB]
  loadDb()

  def loadDb(): Unit = ???

  def checkPass(pass: String): Boolean = {
    val users = UserDB("users")
    val user = users.search(name) //It is a list with users with same name
    if(user.nonEmpty) user.head.password == pass else false
  }

  def changePass(newPass: String): Unit = {
    val users = UserDB()
    users.update(User(name, newPass))
  }
  def delete(): Unit = ???
}


