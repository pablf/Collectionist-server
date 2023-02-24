package DB


import App.Book
import slick.jdbc.H2Profile.api._
import slick.jdbc.H2Profile
import zio.{IO, ZIO}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration



abstract class MarkedTable[Parameter, T](tag: Tag, a: String) extends Table[T](tag, a) {//???
  //use it to mark a parameter??? of the class T
  def marked: Rep[Parameter]
}

trait DB[T <: Serializable, Ts <: Table[T]] {
  val tableQuery: TableQuery[Ts]

  //TODO mejorar Throwable...
  val db: IO[Throwable, H2Profile.backend.JdbcDatabaseDef]

  val connect: IO[Throwable, Future[Unit]] = db.map(_.run(tableQuery.schema.create))



  def add(t: T): IO[Throwable, Unit] = for {
    query <- ZIO.succeed(tableQuery += t)
    database <- db
    result <- ZIO.fromFuture(implicit ec => database.run(query))
  } yield ()




  def update(t: T): IO[Throwable, Unit] = for {
    query <- ZIO.succeed(tableQuery.update(t))
    database <- db
    result <- ZIO.fromFuture(implicit ec => database.run(query))
  } yield ()
}

//TODO === Parameter <: ....
trait MarkedDB[Parameter, T <: Serializable, Ts <: MarkedTable[Parameter, T]] extends DB[T, Ts] {
  def search (searchTerm: Parameter): IO[Throwable, List[T]] = for {
    query <- ZIO.succeed (tableQuery.filter (_.marked === searchTerm).result)
    database <- db
    result <- ZIO.fromFuture (implicit ec => database.run (query) )
  } yield result.toList

  def removeAll(parameter: Parameter): IO[Throwable, Unit] = for {
    query <- ZIO.succeed(tableQuery.filter(_.marked =!= parameter).result)
    database <- db
    result <- ZIO.fromFuture(implicit ec => database.run(query))
  } yield ()
}
