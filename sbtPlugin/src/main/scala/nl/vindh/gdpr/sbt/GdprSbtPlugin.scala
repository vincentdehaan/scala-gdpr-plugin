package nl.vindh.gdpr.sbt

import sbt._
import sbt.Keys._

object GdprSbtPlugin extends AutoPlugin {
  import GdprKeys._

  override def globalSettings: Seq[Def.Setting[_]] = super.globalSettings ++
    Seq(gdprRecordClassName := "nl.vindh.gdpr.DefaultProcessingInstanceRecord")

  override def projectSettings: Seq[Def.Setting[_]] =
    super.projectSettings ++ gdprSettings

  private val scalacPluginName = "gdpr-plugin"

  private lazy val gdprSettings = {
    Seq(
      addCompilerPlugin("nl.vindh" %% "scalac-gdpr" % BuildInfo.version),
      scalacOptions ++= Seq(
        s"-P:$scalacPluginName:reportPath=${crossTarget.value.getAbsolutePath}/gdpr",
        s"-P:$scalacPluginName:recordClassName=${gdprRecordClassName.value}"
      )
    )
  }
}
