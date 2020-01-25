organization := "nl.vindh"

name := "sbt-gdpr"

version := "0.1-SNAPSHOT"

scalaVersion := "2.13.1"

libraryDependencies += "org.scala-sbt" % "sbt" % "1.3.7" % "provided"
libraryDependencies += "nl.vindh" %% "scalac-gdpr" % version.value

enablePlugins(BuildInfoPlugin)
//enablePlugins(SbtPlugin)

buildInfoKeys := Seq[BuildInfoKey](version)
buildInfoPackage := "nl.vindh.gdpr.sbt"