package Validator

import DB.{MarkedStringDB, MarkedTable}
import zio.ZIO

/*
Opens database, check if a parameter exists and validates if another parameter has the same value
Also create a new register (First, Second) in database
Main use: (User, Password)
 */
trait Validator[T <: Serializable, Ts <: MarkedTable[String, T], DB <: MarkedStringDB[T,Ts]] {
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


