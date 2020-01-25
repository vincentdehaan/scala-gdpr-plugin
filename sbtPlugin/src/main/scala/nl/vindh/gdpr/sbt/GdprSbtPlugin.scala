package nl.vindh.gdpr.sbt

import sbt._
import sbt.Keys._

object GdprSbtPlugin extends AutoPlugin {
  //import GdprKeys._

  override def projectSettings: Seq[Def.Setting[_]] =
    super.projectSettings ++ gdprSettings

  private val scalacPluginName = "gdpr-plugin"

  private val gdprSettings = {
    //val path = gdprReportTarget.value
    //println("PATH" + path)
    Seq(
      addCompilerPlugin("nl.vindh" %% "scala-gdpr-plugin" % BuildInfo.version)//,
//      scalacOptions ++= Seq(
    //    s"-P:"//$scalacPluginName:reportPath:TODO",
        //s"-P:$scalacPluginName:recordClassName:TODO"
  //    )
    )
  }
}
