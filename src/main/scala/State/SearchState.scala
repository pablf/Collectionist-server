package State

sealed trait SearchState

object SearchState {
  final case class Search() extends SearchState
  final case class NotFound() extends SearchState
  final case class Found() extends SearchState
}
