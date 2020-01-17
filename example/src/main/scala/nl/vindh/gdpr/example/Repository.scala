package nl.vindh.gdpr.example

import nl.vindh.gdpr.runtime.Processing
import nl.vindh.gdpr.runtime.DataType

object Repository {
  @Processing(DataType.Name)
  def getName(id: String): Option[String] = Some("John Doe")
}
