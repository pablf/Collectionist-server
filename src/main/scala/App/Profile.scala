package App

import DB.{BookDB, User, UserDB}
import Recommendation.Recommender
import zio.{UIO, ZIO}

class Profile(val name: String, val id: Int) {
  // DB of books that make profile of users
  /*
  val readBooks: BookDB = BookDB("readBooks", name ++ "/readBooks")
  val toReadBooks: BookDB = BookDB("toReadBooks", name ++ "/toReadBooks")


  val favouriteBooks: BookDB = BookDB("favouriteBooks", name ++ "/favouriteBooks")
  val likedBooks: BookDB = BookDB("likedBooks", name ++ "/likedBooks")
  val dislikedBooks: BookDB = BookDB("dislikedBooks", name ++ "/dislikedBooks")
  val collectionsBooks: List[BookDB] = loadCollections()
*/


  //TODO
  def loadCollections(): List[BookDB] = {
    List()
  }
}

object Profile {
  def apply(name: String, id: Int): UIO[ Profile] = ZIO.succeed(new Profile(name, id))
}



