import play.Project._

name := """hello-play-java"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.2", 
  "org.webjars" % "bootstrap" % "2.3.1",
  "log4j" % "log4j" % "1.2.17",
  "com.google.inject" % "guice" % "3.0"
    //"com.google.inject" % "guice-assistedinject" % "3.0"
    // Add your own project dependencies in the form:
    // "group" % "artifact" % "version"
)

playJavaSettings
