package State

sealed trait AddState
object AddState {
  final case class Enter(tag: String, added: Boolean) extends AddState
}