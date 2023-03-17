package Login

sealed trait LoginState {

}

object LoginState {
  final case class LoginUser() extends LoginState
  final case class LoginPassword() extends LoginState
  final case class Wrong() extends LoginState
  final case class CreateUser() extends LoginState
  final case class CreatePassword() extends LoginState
}
