package nl.vindh.gdpr.runtime

case class DefaultProcessingInstanceRecord(
  purpose: String,
  subjects: String,
  recipients: String) extends ProcessingInstanceRecord