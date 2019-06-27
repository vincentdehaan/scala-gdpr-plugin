## Befriend the compiler and enhance GDPR compliance
### Vincent de Haan

---

## Who am I?

---

## Goals for today

- Look inside the Scala compiler
- Develop a nice compiler plugin to enhance GDPR compliance

---

@snap[north span-100]
## Art. 30, sect. 1 of the GDPR
@snapend

@snap[west span-50]
> Each controller [...] shall maintain a record of processing activities under its responsibility. That record shall contain all of the following information:
> [...]
> (b) the purposes of the processing;
> (c) [...] the categories of personal data;
> [...]
@snapend

@snap[east span-50]
[ul]
- What is a _processing_?
- How can the compliance officer have _complete_ record of all processings?
[ulend]
@snapend

---

@snap[north span-100]
## The idea
Use @annotations
@snapend

---

@snap[east span-50]
```
object CustomerRepository {
    
    def getCustomerById(id: String) = ???


    def getOrderById(id: String) = ???
}
```

@snap[west span-50]
```
object SomeFeature {
    val customer = getCustomerById(id)

    val order = getOrderById(orderId)    
}
```

---

@snap[east span-50]
```
object CustomerRepository {
    @Processing
    def getCustomerById(id: String) = ???

    @Processing
    def getOrderById(id: String) = ???
}
```
@snapend

@snap[west span-50]
```
object SomeFeature {
    val customer = getCustomerById(id) 
        : @ProcessingInstance(purpose = "Customer support")

    val order = getOrderById(orderId) 
        : @ProcessingInstance(purpose = "Some other purpose")
}
```
@snapend

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

## Inspecting the tree

`scala -Xprint:<phase> -Yshow-trees`

`scala -Ybrowse:<phase>`

---

## Some example code

```
class Processing 
  extends scala.annotation.Annotation {}

class ProcessingInstance(purpose: String) 
  extends scala.annotation.Annotation {}

object Repository {
  @Processing
  def getName(email: String): String = "John doe"
}

object DataProcessing extends App {
  val name = (Repository.getName("john@doe.com") 
  : @ProcessingInstance(purpose = "Customer support")) + "a"

  // This should yield a compiler error!
  val name2 = Repository.getName("foo@bar.com")
}
```

---

## Where do we insert our plugin?

As early as possible, but not earlier

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

```
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

---

### Todo

1. If a method invocation has a `@ProcessingInstance` annotation, print it.

2. If an invocation of a method that is defined with a `@Processing` annotation, does not have an `@ProcessingInstance` annotation, throw a compiler error.

---

### Play with the trees on the REPL

```
import scala.reflect.runtime.universe._
val tree = q"1+1"
showRaw(tree)
```

---

```
override def transform(tree: Tree) = tree match {
      case Typed(appl@ Apply(a, b), tpt) => {
        tpt.asInstanceOf[TypeTree].original match {
          // ...
        }
        super.transform(tree)
      }
```

---

### Quasiquotes as extractors

```
import scala.reflect.runtime.universe._
val tree = q"1+2"
val q"1+$a" = tree
```

---

```
    case Typed(appl@ Apply(a, b), tpt) => {
    tpt.asInstanceOf[TypeTree].original match {
        case q"$expr: @ProcessingInstance(purpose = $purp)" => {
        println(
            s"""
                |Data processing found in ${tree.pos.source}, line ${tree.pos.line}
                | >> $expr
                | Purpose: $purp
            """.stripMargin)
        }
    }
    super.transform(tree)
    }
    ```

---

### Todo

2. If an invocation of a method that is defined with a `@Processing` annotation, does not have an `@ProcessingInstance` annotation, throw a compiler error.

---

```
val annotatedApplies = mutable.Set[Tree]()
// ...
case Typed(appl@ Apply(a, b), tpt) => {
        tpt.asInstanceOf[TypeTree].original match {
          case q"$expr: @ProcessingInstance(purpose = $purp)" => {
            println(
              s"""
                 |Data processing found in ${tree.pos.source}, line ${tree.pos.line}
                 | >> $expr
                 | Purpose: $purp
              """.stripMargin)
            annotatedApplies += appl
          }
        }
        super.transform(tree)
      }
```

---

override def transform(tree: Tree) = tree match {
// ...
      case t@ Apply(a, b) if t.symbol.annotations.toString == "List(Processing)" => { // TODO: not nice!
        if(!annotatedApplies.contains(t)) {
          println(
            s"""
              |ERROR: Data processing found without purpose annotation in ${tree.pos.source}, line ${tree.pos.line}
              | >> $t
            """.stripMargin)
          global.reporter.error(tree.pos, "Unannotated data processing found")
        }
        super.transform(tree)
      }
      case _ =>
      super.transform(tree)
    }

---

## Demo