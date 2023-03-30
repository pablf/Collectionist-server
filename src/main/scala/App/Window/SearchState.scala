package App.Window

sealed trait SearchState extends WindowState

object SearchState {
  final case class NotFound() extends SearchState
  final case class Found() extends SearchState
  final case class AdvancedSearch() extends SearchState
  final case class SimpleSearch() extends SearchState
}
