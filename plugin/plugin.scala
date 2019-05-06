package nl.vindh.gdpr

import scala.tools.nsc.Global
import scala.tools.nsc.Phase
import scala.tools.nsc.plugins._
import scala.tools.nsc.transform._
import scala.tools.nsc.ast.TreeBrowsers
import scala.collection.mutable

class CompilerPlugin(override val global: Global)
  extends Plugin {
  override val name = "compiler-plugin"
  override val description = "Compiler plugin"
  override val components =
    List(new CompilerPluginComponent(global))
}
class CompilerPluginComponent(val global: Global)
  extends PluginComponent with TypingTransformers {
  import global._
  override val phaseName = "compiler-plugin-phase"
  override val runsAfter = List("typer")
  override def newPhase(prev: Phase) =
    new StdPhase(prev) {
      override def apply(unit: CompilationUnit) {
        unit.body = new MyTypingTransformer(unit).transform(unit.body)
      }
    }
  class MyTypingTransformer(unit: CompilationUnit)
    extends TypingTransformer(unit) {
    val annotatedApplies = mutable.Set[Tree]()
    override def transform(tree: Tree) = tree match {
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
  }
  def newTransformer(unit: CompilationUnit) =
    new MyTypingTransformer(unit)
}