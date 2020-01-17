name := "sbtPlugin"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "org.scala-sbt" % "sbt" % "1.3.7" % "provided"
libraryDependencies += "nl.vindh" % "scala-gdpr-plugin" % version.value

enablePlugins(BuildInfoPlugin)

buildInfoKeys := Seq[BuildInfoKey](version)
buildInfoPackage := "nl.vindh.gdpr.sbt"