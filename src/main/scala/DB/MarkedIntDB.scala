package DB

import Common.WithId
import slick.jdbc.PostgresProfile.api._
import zio.{IO, ZIO}

/*
 *  MarkedDB for marked of Int type.
 */

trait MarkedIntDB[T <: WithId, Ts <: MarkedTable[Int, T]] extends MarkedDB[Int, T, Ts] {

  override def search (searchTerm: Int): IO[Throwable, List[T]] = {
    val query = tableQuery.filter(_.marked === searchTerm).result
    ZIO.fromFuture(implicit ec => db.run(query)).map(_.toList)
  }

  override def removeAll(parameter: Int): IO[Throwable, Unit] = {
    val query = tableQuery.filter(_.marked === parameter).delete
    ZIO.fromFuture(implicit ec => db.run(query)) *> ZIO.unit
  }

}