package Validator

import DB.{MarkedStringDB, MarkedTable}
import zio.{ZIO, ZLayer}

/*
Opens database, check if a parameter exists and validates if another parameter has the same value
Also create a new register (First, Second) in database
Main use: (User, Password)
 */
trait Validator[T <: Serializable, Ts <: MarkedTable[String, T], DB <: MarkedStringDB[T,Ts]] {
  val db: DB

  def checkFirst(tag: String): ZIO[Any, Throwable, Boolean]

  def checkSecond(tag: String): ZIO[Any, Throwable, Boolean]

  def addFirst(tag: String): ZIO[Any, Throwable, Boolean]

  def addSecond(tag: String): ZIO[Any, Throwable, Boolean]

  def addBoth(first: String, second: String): ZIO[Any, Throwable, Boolean]

  def delete(first: String): ZIO[Any, Throwable, Unit]
}


