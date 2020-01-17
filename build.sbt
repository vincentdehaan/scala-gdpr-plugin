lazy val runtime = project in file("runtime")

lazy val scalacPlugin = project in file("scalacPlugin")

lazy val sbtPlugin = project in file("sbtPlugin")

lazy val example = (project in file("example"))
  .settings(publish := {})