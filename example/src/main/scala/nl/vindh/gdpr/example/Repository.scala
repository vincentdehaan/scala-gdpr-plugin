package nl.vindh.gdpr.example

object Repository {
  @ProcessingInstance
  def getPersonalData(id: String): Option[String] = Some("This is some personal data")
}
