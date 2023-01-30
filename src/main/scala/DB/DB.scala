package DB


import slick.jdbc.H2Profile.api._

trait DB[T] {
  type TType <: Table[T]
  val tableQuery: TableQuery[TType]
  val conf: String
  val db: Database = Database.forConfig(conf)

  def search(searchTerm: String): List[T]

}
