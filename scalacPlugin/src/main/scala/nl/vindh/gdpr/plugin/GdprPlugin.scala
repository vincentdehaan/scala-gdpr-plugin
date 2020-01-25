package nl.vindh.gdpr.plugin

import scala.tools.nsc.Global
import scala.tools.nsc.plugins._

class GdprPlugin(override val global: Global)
  extends Plugin {
  override val name = "gdpr-plugin"
  override val description = "Compiler plugin to enhance GDPR compliance"
  override val components =
    List(new GdprPluginComponent(global))
}