import play.Project._

name := """hello-play-java"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.2",
  "org.webjars" % "bootstrap" % "2.3.1",
  "log4j" % "log4j" % "1.2.17",
  "com.google.inject" % "guice" % "3.0",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1",
  "commons-io" % "commons-io" % "2.4",
  "com.google.guava" % "guava" % "10.0.1",
  "org.pac4j" % "play-pac4j_java" % "1.3.0",
  "org.pac4j" % "pac4j-oauth" % "1.6.0"
    // "group" % "artifact" % "version"
)

playJavaSettings
