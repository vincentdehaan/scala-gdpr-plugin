package nl.vindh.gdpr.runtime

sealed trait DataType

object DataType {

  case object Name extends DataType

  case object Age extends DataType

  case object Address extends DataType

  case class Other(desc: String) extends DataType

}