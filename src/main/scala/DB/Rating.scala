package DB

case class Rating(val user: Int, val item: Int, val rating: Int) extends Serializable

