package Validator

import Common.WithId
import DB.{MarkedStringDB, MarkedTable}
import zio.ZIO

/*
 * A Validator opens a database, checks if a parameter exists and validates if another parameter has the same value.
 * It also creates a new register (First, Second) in database.
 * Main use with UserDB and (User, Password).
 */
trait Validator[T <: WithId, Ts <: MarkedTable[String, T], DB <: MarkedStringDB[T,Ts]] {
  val db: DB

  def existFirst(tag: String): ZIO[Any, Throwable, Boolean]

  def checkFirst(tag: String): ZIO[Any, Throwable, Boolean]

  def checkSecond(tag: String): ZIO[Any, Throwable, Boolean]

  def tryLogin(fst: String, snd: String): ZIO[Any, Throwable, Boolean]

  def add(fst: String, snd: String): ZIO[Any, Throwable, Boolean]

  def change(fst: String, snd: String): ZIO[Any, Throwable, Boolean]

  def id(fst: String): ZIO[Any, Throwable, Int]

  def delete(fst: String): ZIO[Any, Throwable, Unit]
}


