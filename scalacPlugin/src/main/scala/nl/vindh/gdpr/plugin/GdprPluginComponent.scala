package nl.vindh.gdpr.plugin

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}

import scala.tools.nsc.Phase
import scala.tools.nsc.plugins._
import scala.tools.nsc.transform._
import scala.tools.nsc.Global
import scala.collection.mutable

class GdprPluginComponent(val global: Global, options: PluginOptions)
  extends PluginComponent with TypingTransformers {
  import global._
  override val phaseName = "gdpr-annotation-checker"
  override val runsAfter = List("typer")
  override def newPhase(prev: Phase) =
    new StdPhase(prev) {
      override def apply(unit: CompilationUnit) {
        unit.body = new GdprTraverser(unit).transform(unit.body)
      }
    }
  class GdprTraverser(unit: CompilationUnit)
    extends TypingTransformer(unit) {
    val annotatedApplies = mutable.Set[Tree]()
    private val report = {
      val f = new File(options.reportPath, "gdpr.csv")
      f.getParentFile.mkdirs()
      f.createNewFile()
      f
    }

    override def transform(tree: Tree) = tree match {
      case Typed(appl@ Apply(a, b), tpt) => {
        tpt.asInstanceOf[TypeTree].original match {
          case q"$expr: @ProcessingInstance(DefaultProcessingInstanceRecord($purpose, $subjects, $recipients))" => {
             // TODO: support non-default record types
            writeToFile(report, s"${tree.pos.source},${tree.pos.line},$purpose,$subjects,$recipients")
            annotatedApplies += appl
          }
          case _ =>
        }
        super.transform(tree)
      }
      case t @ Apply(_, _)
        if t.symbol.annotations.toString.contains("nl.vindh.gdpr.runtime.Processing") => { // TODO: not nice!
          if(!annotatedApplies.contains(t)) {
            global.reporter.error(tree.pos, "Unannotated data processing found")
          }
          super.transform(tree)
        }
      case _ =>
        super.transform(tree)
    }

    def writeToFile(file: File, str: String) = {
      val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))
      try {
        writer.write(str)
      } finally {
        writer.close()
      }
    }
  }
  def newTransformer(unit: CompilationUnit) =
    new GdprTraverser(unit)
}