package DB

import slick.jdbc.H2Profile.api._


abstract class MarkedTable[Parameter, T](tag: Tag, a: String) extends Table[T](tag, a) {//???

  def marked: Rep[Parameter]

}

//TODO === Parameter <: ....

trait MarkedDB[Parameter, T <: Serializable, Ts <: MarkedTable[Parameter, T]] extends DB[T, Ts]