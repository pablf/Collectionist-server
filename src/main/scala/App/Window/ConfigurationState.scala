package App.Window

sealed trait ConfigurationState extends WindowState
object ConfigurationState {
  final case class AskCurrentPassword() extends ConfigurationState
  final case class AskNewPassword() extends ConfigurationState
  final case class Menu() extends ConfigurationState
}