package DB

import Common.WithId
import slick.jdbc.PostgresProfile.api._
import zio.{IO, ZIO}

/*
 *  MarkedDB for marked of String type.
 */

trait MarkedStringDB[T <: WithId, Ts <: MarkedTable[String, T]] extends MarkedDB[String, T, Ts] {

  override def search(searchTerm: String): IO[Throwable, List[T]] = {
    val query = tableQuery.filter(_.marked === searchTerm).result
    ZIO.fromFuture(implicit ec => db.run(query)).map(_.toList)
  }

  override def removeAll(parameter: String): IO[Throwable, Unit] = {
    val query = tableQuery.filter(_.marked === parameter).delete
    ZIO.fromFuture(implicit ec => db.run(query)) *> ZIO.unit
  }

}