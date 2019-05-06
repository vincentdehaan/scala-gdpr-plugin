// See also https://medium.com/@adrian.n/scala-compiler-plugin-annotation-based-method-ast-rewriting-wrapping-substitution-b802f2d922f1

class wrapThisMethod extends scala.annotation.StaticAnnotation {}

class Processing extends scala.annotation.Annotation {

}

class ProcessingInstance(purpose: String) extends scala.annotation.Annotation {

}

object Repository {
  @Processing
  def getName(email: String): String = "John doe"

  @Processing
  def getPhoneNumber(email: String): String = "12345"
}

object DataProcessing extends App {
  val name = (Repository.getName("john@doe.com") : @ProcessingInstance(purpose = "Customer support")) + "a"

val name3 = Repository.getName("john@doe.com")

  val name2 = Repository.getName("foo@bar.com")
  @wrapThisMethod def f = 1



  println(f)
}