package Mode

sealed trait ModeType
object ModeType {
  final class LoginType extends ModeType
  final class AppType extends ModeType
  final class ExitType extends ModeType
}
