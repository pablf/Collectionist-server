package State

sealed trait AppState {

}

object AppState {
  final case class GetUser() extends AppState
  final case class GetPassword() extends AppState
  final case class Wrong() extends AppState
  final case class CreateUser() extends AppState
  final case class CreatePassword() extends AppState
}
