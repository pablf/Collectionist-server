package Common

/*
 * Template class for the objects of PlainBookDB. It represents a book without the state that has in the library.
 */

case class PlainBook(
                      val name: String = "na",
                      val author: String = "na",
                      val genre : String = "na",
                      override val id : Int = 0
                    ) extends WithId
