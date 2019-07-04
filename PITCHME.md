## Befriend the compiler and enhance GDPR compliance
### Vincent de Haan

---

## Who am I?

---

## Goals for today

- Look inside the Scala compiler
- Develop a nice compiler plugin to enhance GDPR compliance

---

### Art. 30, sect. 1 of the GDPR

@snap[text-left]
Each controller [...] shall maintain a record of *processing* activities under its responsibility. That record shall contain all of the following information:

[...]

(b) the purposes of the processing;

(c) [...] the categories of personal data;

[...]
@snapend

---

@snap[north span-100]
## The idea
Use @annotations
@snapend

---

```scala
object CustomerRepository {
  @Processing
  def getCustomerById(id: String) = ???

  @Processing
  def getOrderById(id: String) = ???
}

object SomeFeature {
  val customer = CustomerRepository.getCustomerById(id) 
    : @ProcessingInstance(purpose = "Customer support")

  val order = CustomerRepository.getOrderById(orderId) 
    : @ProcessingInstance(purpose = "Some other purpose")
}
```
@[1,3,6-10,13,15]()
@[2-3, 5-6](Mark the repository methods that indicate a processing)
@[10-11,13-14](Mark the individual processings)

---

### Todo

1. If a method invocation has a `@ProcessingInstance` annotation, print it.

2. If an invocation of a method that is defined with a `@Processing` annotation, does not have a `@ProcessingInstance` annotation, throw a compiler error.

---

## How does the compiler work?
Abstract syntax tree transformation

---

@snap[west snap-50]
```
val x = 1 + 2 * 3
```
@snapend
@snap[east snap-50]
```
ValDef(
  0,
  "x",
  <tpt>,
  Apply(
    1."$plus",
    Apply(
      2."$times",
      3
    )
  )
)
```

---

```
$ scala -Xshow-phases
    phase name  id  description
    ----------  --  -----------
        parser   1  parse source into ASTs, perform simple desugaring
         namer   2  resolve names, attach symbols to named trees
packageobjects   3  load package objects
         typer   4  the meat and potatoes: type the trees
        patmat   5  translate match expressions
 
 ...

           jvm  23  generate JVM bytecode
      terminal  24  the last phase during a compilation run
```

---

## Where do we insert our plugin?

As early as possible, but not earlier

Inspect the trees!

---

## Inspecting the tree
@snap[text-left]
`scala -Xprint:<phase> -Yshow-trees`
@snapend

@snap[text-left]
`scala -Ybrowse:<phase>`
@snapend

---

## Some example code

```scala
class Processing 
  extends scala.annotation.Annotation {}

class ProcessingInstance(purpose: String) 
  extends scala.annotation.Annotation {}

object Repository {
  @Processing
  def getName(email: String): String = "John doe"
}

object DataProcessing extends App {
  val name = Repository.getName("john@doe.com") 
  : @ProcessingInstance(purpose = "Customer support")

  val name2 = Repository.getName("foo@bar.com")
}
```
@[1-5](The annotations)
@[8-9](A data processing method)
@[13-14](Correct usage)
@[16](This should yield a compiler error!)

---

```
$ scala -Xshow-phases
    phase name  id  description
    ----------  --  -----------
        parser   1  parse source into ASTs, perform simple desugaring
         namer   2  resolve names, attach symbols to named trees
packageobjects   3  load package objects
         typer   4  the meat and potatoes: type the trees
        patmat   5  translate match expressions
 
 ...

           jvm  23  generate JVM bytecode
      terminal  24  the last phase during a compilation run
```

---

## Plugin skeleton

See https://docs.scala-lang.org/overviews/plugins/index.html

---

```scala
class CompilerPlugin(override val global: Global)
  extends Plugin {
  override val name = "gdpr-plugin"
  override val description = "Compiler plugin to enhance GDPR compliance"
  override val components =
    List(new CompilerPluginComponent(global))
}
class CompilerPluginComponent(val global: Global)
  extends PluginComponent with TypingTransformers {
  import global._
  override val phaseName = "gdpr-annotation-checker"
  override val runsAfter = List("typer")
  override def newPhase(prev: Phase) =
    new StdPhase(prev) {
      override def apply(unit: CompilationUnit) {
        unit.body = new MyTypingTransformer(unit).transform(unit.body)
      }
    }
  class MyTypingTransformer(unit: CompilationUnit)
    extends TypingTransformer(unit) {
// ...
      override def transform(tree: Tree) = // ...
  }
  def newTransformer(unit: CompilationUnit) =
    new MyTypingTransformer(unit)
}

```
@[3-4,11](Plugin metadata)
@[12](Where to insert the plugin?)
@[22](Here the magic happens!)

---

### Todo

1. If a method invocation has a `@ProcessingInstance` annotation, print it.

2. If an invocation of a method that is defined with a `@Processing` annotation, does not have a `@ProcessingInstance` annotation, throw a compiler error.

---

### Play with the trees on the REPL

```scala
import scala.reflect.runtime.universe._
val tree = q"1+1"
showRaw(tree)
```

---?code=plugin/plugin.scala?lang=scala&title=Catch the `@ProcessingInstance`

@[31-32,45-46](Match on the Typed(Apply, _)) pattern)
@[32](What is in `tpt`?)

---

```scala
case Typed(appl@ Apply(a, b), tpt) => {
  println(showRaw(tpt))
  // ...
}
/*
TypeTree().setOriginal(
  Annotated(
    Apply(
      Select(New(Ident(ProcessingInstance)), termNames.CONSTRUCTOR), 
      List(
        AssignOrNamedArg(Ident(TermName("purpose")), 
        Literal(Constant("Customer support"))))), 
    Apply(
      Select(Ident(Repository), TermName("getName")), 
      List(Literal(Constant("john@doe.com"))))))
*/
```
@[1-2](Use `showRaw` to look inside `tpt`)
@[6-15](Let's guess `tpt.asInstanceOf[TypeTree].original`)


---

### Extract a complicated tree

```scala
/*
val tree = Annotated(
    Apply(
      Select(New(Ident(ProcessingInstance)), termNames.CONSTRUCTOR), 
      List(
        AssignOrNamedArg(Ident(TermName("purpose")), 
        Literal(Constant("Customer support"))))), 
    Apply(
      Select(Ident(Repository), TermName("getName")), 
      List(Literal(Constant("john@doe.com")))))
*/
tree match {
  case Annotated(Apply(Select(New(Ident(ProcessingInstance)), _), ...
}
```

---

### Quasiquotes as extractors

```scala
import scala.reflect.runtime.universe._
val tree = q"1+2"
val q"1+$a" = tree
```

---?code=plugin/plugin.scala?lang=scala&title=Catch the `@ProcessingInstance`

@[33-44](Match using quasiquotes)
@[32-46](Rule #1 has been implemented)

---

### Todo

@snap[text-left]
2 . If an invocation of a method that is defined with a `@Processing` annotation, does not have a `@ProcessingInstance` annotation, throw a compiler error.
@snapend

---?code=plugin/plugin.scala?lang=scala&title=Catch the unannotated processing

@[47](Catch all processings)
@[30,41](Keep track of the annotated processings)
@[47-49](Filter the unannotated processings)
@[50-55](Report the error)
@[47-58](Rule #2 has been implemented)

---

## Demo

---

## Looking ahead

- File output
- Sbt plugin
- Scala 3 / Dotty