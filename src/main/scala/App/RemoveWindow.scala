package App
/*
case class RemoveWindow(val mode: AppMode) extends Window {
  val name = "Remove Book"
  var state: RemoveState = RemoveState.Enter("name", false)
  val fields = List("name", "author", "genre")
  val bookMap: Map[String, String] = Map("name" -> "", "author" -> "", "genre" -> "")



  def print(): ZIO[Any, IOException, Unit] = for {
    _ <- state match {
      case RemoveState.Enter(_, true) => printLine(s"   Your book ${bookMap("name")} was Removeed correctly. Remove a new book! Enter the parameters:")
      case RemoveState.Enter(_, false) => printLine("   Remove a new book! Enter the parameters:")
    }
    _ <- ZIO.foreach(fields)(field => printLine(s"${printSelector(field)} field: ${bookMap(field)}"))
    _ <- printLine("     |Remove Book [G]|")
  } yield ()

  def printSelector(field: String): String = if(isSelectedField(field)) "  ->  " else "    "

  def isSelectedField(field: String): Boolean = state match {
      case RemoveState.Enter(tag, _) => tag == field
      case _ => false
    }

  def printKeymap(): ZIO[Any, IOException, Unit] = printLine("Select fields with arrow. [A] to Remove book")

  def keymap(tag: String): Event[AppType] = tag match {
    case "u" => RemoveEvent.SelectField(true)
    case "d" => RemoveEvent.SelectField(false)
    case "G" => RemoveEvent.RemoveBook()
    case _ => RemoveEvent.SetBook(tag)
  }

  trait RemoveEvent extends ExEvent[AppType]
  val NAE = NullEvent[AppType]()

  object RemoveEvent {
    case class SetBook(tag: String) extends RemoveEvent {
      def execute(): Event[AppType] = {
        state match {
          case RemoveState.Enter(field, _) => bookMap(field) = tag
        }
        NAE
      }
    }




    case class RemoveBook() extends RemoveEvent {
      def execute(): Event[AppType] = {
        mode.bookdb.Remove(Book(bookMap))
        state = state match {
          case RemoveState.Enter(field, _) => RemoveState.Enter(field, true)
        }
        NAE
      }
    }

    case class SelectField(up: Boolean) extends RemoveEvent {
      def execute(): Event[AppType] = if(up){
        state = state match {
          case RemoveState.Enter("genre", v) => RemoveState.Enter("author", v)
          case RemoveState.Enter(_, v) => RemoveState.Enter("name", v)
        }
        NAE
      } else {
        state = state match {
          case RemoveState.Enter("name", v) => RemoveState.Enter("author", v)
          case RemoveState.Enter(_, v) => RemoveState.Enter("genre", v)
        }
        NAE
      }
    }


  }

}
*/