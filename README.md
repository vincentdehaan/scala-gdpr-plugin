Scala Compiler plugin for GDPR compliance
---

The General Data Protection Regulation states (art. 30) that each controller shall have a record of all it's processings of personal data. This means that the data protection officer needs to be aware of all data processings that happen within an organization. Since most of these processings usually take place within IT systems and the data protection officer is in general not an expert in the field of these systems, he will have hard time being sure that his record is complete.

The plugin in this repository can help find and catalogue processings of personal data. The assumption is that a processing of personal data will usually coincide with an action upon a database, and that in most codebaes this interaction is abstracted away in some kind of a 'repository'. Now the idea is as follows:
* Mark the repository methods with a `@Processing` annotation. Since the repository code won't change regularly, this can be done only once by a senior devoper who understands the importance of GDPR compliance.
* Whenever a (junior) developer wants to implement some new feature and he needs to use an existing repository method, he needs to annotate this at the call site with a `@ProcessingInstance` annotation. This annotation allows him to specify a purpose of the specific processing as required by the GDPR.
* When compiling the project, the plugin checks whether each call to a `@Processing` annotated method has a `@ProcessingInstance` annotation next to it. It prints out all purposes so that the compliance officer can inspect those, and judge their legality. This way it is possible to export the record of processings directly from the code.

## Compiling and running the plugin

At first, make sure that the plugin is available in the local Ivy repository:

```
sbt +runtime/publishLocal
sbt +scalacPlugin/publishLocal
sbt sbtPlugin/publishLocal
```

Add the plugin to `project/plugins.sbt`:

```scala
addSbtPlugin("nl.vindh" %% "sbt-gdpr" % "0.1-SNAPSHOT")
```

Add these lines to `build.sbt`, enabling the plugin and importing the runtime:
```scala
libraryDependencies += "nl.vindh" %% "scala-gdpr-runtime" % "0.1-SNAPSHOT"

enablePlugins(GdprSbtPlugin)
```

TODO
---
- Report the data type of the `@Processing` annotation.
- Allow custom `ProcessingInstanceRecord` types.
- Support Scala 2.11 and 2.13.