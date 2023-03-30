package App.Window

sealed trait AddState extends WindowState
object AddState {
  final case class Enter(tag: String, added: Boolean) extends AddState
}