class Processing extends scala.annotation.Annotation {

}

class ProcessingInstance(purpose: String) extends scala.annotation.Annotation {

}

object Repository {
  @Processing
  def getName(email: String): String = "John doe"
}

object DataProcessing extends App {
  val name = Repository.getName("john@doe.com") : @ProcessingInstance(purpose = "Customer support")

  // This should yield a compiler error!
  //val name2 = Repository.getName("john@doe.com")
}