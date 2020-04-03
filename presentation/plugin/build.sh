scalac -d classes plugin.scala
cd classes
jar cf ../gdpr.jar .
cd ..