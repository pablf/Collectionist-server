package App

import DB.{BookDB, User, UserDB}
import Recommendation.Recommender

class Profile(val name: String, val id: Int) {
  // DB of books that make profile of users
  val readBooks: BookDB = BookDB("readBooks", name ++ "/readBooks")
  val toReadBooks: BookDB = BookDB("toReadBooks", name ++ "/toReadBooks")


  val favouriteBooks: BookDB = BookDB("favouriteBooks", name ++ "/favouriteBooks")
  val likedBooks: BookDB = BookDB("likedBooks", name ++ "/likedBooks")
  val dislikedBooks: BookDB = BookDB("dislikedBooks", name ++ "/dislikedBooks")
  val collectionsBooks: List[BookDB] = loadCollections()



  //TODO
  def loadCollections(): List[BookDB] = {
    List()
  }

  def checkPass(pass: String): Boolean = {
    val users = UserDB("users")
    val user = users.search(name) //It is a list with users with same name
    if(user.nonEmpty) user.head.password == pass else false
  }

  def changePass(newPass: String): Unit = {
    val users = UserDB("USERS")
    users.update(User(name, newPass, id))
  }
  def delete(): Unit = ???
}



