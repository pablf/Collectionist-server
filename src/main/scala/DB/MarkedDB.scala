package DB

import Common.WithId
import slick.jdbc.PostgresProfile.api.{Rep, Table, Tag}
import zio.IO

/*
 *  A MarkedTable signals a particular parameter.
 *
 *  A MarkedDB is a DB using a MarkedTable. It declares methods:
 *     - search,
 *     - removeAll,
 *  using only the marked parameter. Implementations are in subclasses of trait MarkedDB because
 *  slick might not be able to apply === and =!= for arbitrary Parameter.
 */

abstract class MarkedTable[Parameter, T <: WithId](tag: Tag, a: String) extends TableWithId[T](tag, a) {

  def marked: Rep[Parameter]

}

trait MarkedDB[Parameter, T <: WithId, Ts <: MarkedTable[Parameter, T]] extends DB[T, Ts] {

  def search (searchTerm: Parameter): IO[Throwable, List[T]]

  def removeAll(parameter: Parameter): IO[Throwable, Unit]

}