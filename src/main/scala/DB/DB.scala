package DB

import Common.WithId
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.PostgresProfile
import slick.sql.SqlProfile.ColumnOption.SqlType
import zio.{IO, Task, ZIO}

/*
 *  A DB is an usual database. Each database corresponds with a class in package Common to interact with client.
 *  It implements methods:
 *     - add
 *     - update
 *     - find (by Id)
 *
 *
 *
 *  MarkedDB are used to have an special "marked" parameter.
 */

abstract class TableWithId[T <: WithId](tag: Tag, name: String) extends Table[T](tag, name) {
  def id = column[Int]("ID", SqlType("SERIAL"), O.PrimaryKey, O.AutoInc)
}


trait DB[T <: WithId, Ts <: TableWithId[T]] {
  val tableQuery: TableQuery[Ts]

  val db: PostgresProfile.backend.JdbcDatabaseDef

  def add(t: T): IO[Throwable, Any] = {
    val query2 = tableQuery returning tableQuery.map(_.id)
    val query = tableQuery.insertOrUpdate(t)
    ZIO.fromFuture(implicit ec => db.run(query2 += t))
  }

  def update(t: T): IO[Throwable, Int] = {
    val query = tableQuery.update(t)
    ZIO.fromFuture(implicit ec => db.run(query))
  }

  // Find element with id. It is Nil if there is none and List(x) if there is one.
  def find(id: Int): IO[Throwable, List[T]] = {
    val query = tableQuery.filter(_.id === id).result
    ZIO.fromFuture(implicit ec => db.run(query)).map(_.toList)
  }

  // Find elements with id in ids.
  def findList(ids: List[Int]): IO[Throwable, List[T]] = ZIO
    .collectAll(ids.map(n => find(n)))
    .map(_.flatten)

  def delete(id: Int): IO[Throwable, Int] =
    ZIO.fromFuture(implicit ec => db.run(tableQuery.filter(_.id === id).delete))

}