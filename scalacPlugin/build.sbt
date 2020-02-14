organization := "nl.vindh"

name := "scalac-gdpr"

version := "0.1-SNAPSHOT"

crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"

