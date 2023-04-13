package DB

import slick.jdbc.H2Profile.api._
import zio.{IO, ZIO}

/*
 *
 */

trait MarkedIntDB[T <: Serializable, Ts <: MarkedTable[Int, T]] extends MarkedDB[Int, T, Ts] {

  override def search (searchTerm: Int): IO[Throwable, List[T]] = for {
    query <- ZIO.succeed (tableQuery.filter (_.marked === searchTerm).result)
    result <- ZIO.fromFuture(implicit ec => db.run(query))
  } yield result.toList

  override def removeAll(parameter: Int): IO[Throwable, Unit] = for {
    query <- ZIO.succeed(tableQuery.filter(_.marked =!= parameter).result)
    _ <- ZIO.fromFuture(implicit ec => db.run(query))
  } yield ()

}
