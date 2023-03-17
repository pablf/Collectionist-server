package DB

import slick.jdbc.H2Profile.api._
import slick.jdbc.H2Profile

import zio.{IO, ZIO}


trait DB[T <: Serializable, Ts <: Table[T]] {
  //class type required but Ts found ???
  val tableQuery: TableQuery[Ts]

  //TODO mejorar Throwable...
  val db: H2Profile.backend.JdbcDatabaseDef

  def add(t: T): IO[Throwable, Unit] = for {
    query <- ZIO.succeed(tableQuery += t)
    _ <- ZIO.fromFuture(implicit ec => db.run(query))
  } yield ()

  def update(t: T): IO[Throwable, Unit] = for {
    query <- ZIO.succeed(tableQuery.update(t))
    _ <- ZIO.fromFuture(implicit ec => db.run(query))
  } yield ()
}

