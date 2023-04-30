package Common

import java.time.LocalDate

/*
 * Template class for the objects of UserDB. It represents a user of Collectionist.
 */

case class User(
                 val name: String,
                 val password: String,
                 val mail : String = "",
                 val permissionLevel : Int = 100,
                 val banDate : LocalDate = LocalDate.MIN,
                 override val id: Int = 0
               ) extends WithId
