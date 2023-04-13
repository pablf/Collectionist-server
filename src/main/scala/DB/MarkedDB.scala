package DB

import slick.jdbc.H2Profile.api.{Tag, Table, Rep}
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

abstract class MarkedTable[Parameter, T](tag: Tag, a: String) extends Table[T](tag, a) {//???

  def marked: Rep[Parameter]

}

//TODO === Parameter <: ....

trait MarkedDB[Parameter, T <: Serializable, Ts <: MarkedTable[Parameter, T]] extends DB[T, Ts] {

  def search (searchTerm: Parameter): IO[Throwable, List[T]]

  def removeAll(parameter: Parameter): IO[Throwable, Unit]

}