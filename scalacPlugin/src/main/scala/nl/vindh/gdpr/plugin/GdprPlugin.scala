package nl.vindh.gdpr.plugin

import scala.tools.nsc.Global
import scala.tools.nsc.plugins._

class GdprPlugin(override val global: Global)
  extends Plugin {
  private val parsedOptions = new PluginOptions
  override val name = "gdpr-plugin"
  override val description = "Compiler plugin to enhance GDPR compliance"
  override val components =
    List(new GdprPluginComponent(global, parsedOptions))

  private def getOption(opts: List[String], key: String): Option[String] =
    opts.find {
      _.startsWith(s"$key=")
    }.map(_.stripPrefix(s"$key="))

  override def init(options: scala.List[_root_.scala.Predef.String] = super.options, error: String => Unit): Boolean = {
    parsedOptions.recordClassName = getOption(options, "recordClassName").getOrElse {
      error("Not specified: recordClassName")
      ""
    }
    parsedOptions.reportPath = getOption(options, "reportPath").getOrElse {
      error("Not specified: reportPath")
      ""
    }
    true
  }
}