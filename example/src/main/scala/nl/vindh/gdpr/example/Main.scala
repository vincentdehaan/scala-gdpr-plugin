package nl.vindh.gdpr.example

import nl.vindh.gdpr.runtime.{DefaultProcessingInstanceRecord, ProcessingInstance}

object Main extends App {
  val name = Repository.getName("i123") : @ProcessingInstance(DefaultProcessingInstanceRecord("Customer service", "customers", "employees"))
  println(s"The name of customer i123 is $name")
}
