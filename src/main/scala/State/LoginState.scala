package State

sealed trait LoginState {

}

object LoginState {
  final case class GetUser() extends LoginState
  final case class GetPassword() extends LoginState
  final case class Wrong() extends LoginState
  final case class CreateUser() extends LoginState
  final case class CreatePassword() extends LoginState
}
