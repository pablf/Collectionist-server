package DB

case class User(val name: String, val password: String, val id: Int) extends Serializable
