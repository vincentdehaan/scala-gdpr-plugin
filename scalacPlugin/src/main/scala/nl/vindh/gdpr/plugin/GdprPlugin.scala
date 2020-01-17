package nl.vindh.gdpr.plugin

import scala.tools.nsc._
import scala.tools.nsc.plugins._
import scala.tools.nsc.transform._

class GdprPlugin(override val global: Global)
  extends Plugin {
  override val name = "gdpr-plugin"
  override val description = "Compiler plugin to enhance GDPR compliance"
  override val components =
    List(new CompilerPluginComponent(global))
}
