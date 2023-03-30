package App.Window

sealed trait RemoveState extends WindowState
object RemoveState {
  final case class Enter(tag: String, Removeed: Boolean) extends RemoveState
}