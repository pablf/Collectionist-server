package DB

import slick.jdbc.H2Profile.api._
import zio.{IO, ZIO}

trait MarkedStringDB[T <: Serializable, Ts <: MarkedTable[String, T]] extends MarkedDB[String, T, Ts] {
  def search (searchTerm: String): IO[Throwable, List[T]] = for {
    query <- ZIO.succeed (tableQuery.filter (_.marked === searchTerm).result)
    result <- ZIO.fromFuture(implicit ec => db.run(query))
  } yield result.toList

  def removeAll(parameter: String): IO[Throwable, Unit] = for {
    query <- ZIO.succeed(tableQuery.filter(_.marked =!= parameter).result)
    _ <- ZIO.fromFuture(implicit ec => db.run(query))
  } yield ()
}