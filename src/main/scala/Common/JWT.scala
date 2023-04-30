package Common

import zio.Task

/*
 * TODO
 * JWT that encodes and decodes tokens for authorization in REST API.
 */

object JWT extends scala.AnyRef {

  def decode(token : String): String = token
  def encode(token : String, permissionLevel : Int): String = token + "/" +permissionLevel.toString

  // decodes a token and checks if it has been produced with permission or higher as PermissionLevel of the user.
  def isValid(permission : Int, s : Task[String]) : Task[Boolean] =
    s.map(permission <= getEnd(_).toInt)

  // Gets that the end of s after "/"
  def getEnd(s: String): String = {
    if(s.last == '/') "" else getEnd(s.init) + s.last
  }

}

