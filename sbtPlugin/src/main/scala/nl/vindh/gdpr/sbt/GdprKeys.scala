package nl.vindh.gdpr.sbt

import sbt._

object GdprKeys {
  lazy val gdprRecordClassName = settingKey[String]("The class name of the ProcessingInstanceRecord")
}
