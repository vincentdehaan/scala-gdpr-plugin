package nl.vindh.gdpr.sbt

import sbt._
import sbt.Keys._

object GdprSbtPlugin extends AutoPlugin {
  override def projectSettings: Seq[Def.Setting[_]] =
    super.projectSettings ++ gdprSettings

  private val gdprSettings =
    Seq(
      addCompilerPlugin("nl.vindh" %% "scala-gdpr-plugin" % BuildInfo.version),
      scalacOptions ++= Seq(
        s"-P:"
      )
    )
}
