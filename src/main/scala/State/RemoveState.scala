package State

sealed trait RemoveState
object RemoveState {
  final case class Enter(tag: String, Removeed: Boolean) extends RemoveState
}