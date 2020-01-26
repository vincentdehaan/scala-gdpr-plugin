package nl.vindh.gdpr.example

object Main extends App {
  val name = Repository.getName("i123")
  println(s"The name of customer i123 is $name")
}
