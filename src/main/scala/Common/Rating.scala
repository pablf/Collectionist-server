package Common

/*
 * Template class for the objects of RatingDB. It represents the rating of a book by an user. It is employed
 * by Recommendation service.
 */

case class Rating(
                   val user: Int,
                   val item: Int,
                   val rating: Int
                 ) extends WithId
