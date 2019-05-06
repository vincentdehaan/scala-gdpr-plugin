Scala Compiler plugin for GDPR compliance
---

The General Data Protection Regulation states (art. 30) that each controller shall have a record of all it's processings of personal data. This means that the data protection officer needs to be aware of all data processings that happen within an organization. Since most of these processings usually take place within IT systems and the data protection officer is in general not an expert in the field of these systems, he will have hard time being sure that his record is complete.

The plugin in this repository can help find and catalogue processings of personal data. The assumption is that a processing of personal data will usually coincide with an action upon a database, and that in most codebaes this interaction is abstracted away in some kind of a 'repository'. Now the idea is as follows:
* Mark the repository methods with a `@Processing` annotation. Since the repository code won't change regularly, this can be done only once by a senior devoper who understands the importance of GDPR compliance.
* Whenever a (junior) developer wants to implement some new feature and he needs to use an existing repository method, he needs to annotate this at the call site with a `@ProcessingInstance` annotation. This annotation allows him to specify a purpose of the specific processing as required by the GDPR.
* When compiling the project, the plugin checks whether each call to a `@Processing` annotated method has a `@ProcessingInstance` annotation next to it. It prints out all purposes so that the compliance officer can inspect those, and judge their legality. This way it is possible to export the record of processings directly from the code.

## Compiling and running the plugin

In the `plugin` folder:
```
scalac -d classes plugin.scala
cd classes
jar cf ../gdpr.jar .
```
Or run `build.sh`.

In the source folder:
```
scalac -Xplugin:./plugin/gdpr.jar -Xplugin-list # shows that the plugin is loaded correctly
scalac -Xplugin:./plugin/gdpr.jar data.scala
```