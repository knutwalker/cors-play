name := """cors-play"""

// version is set by sbt-release

organization := "de.knutwalker"

organizationName := "knutwalker"

organizationHomepage := Some(url("https://knutwalker.de/"))

description := """Easy CORS support for Play"""

homepage := Some(url("http://knutwalker.de/cors-play/"))

startYear := Some(2014)

scalaVersion := "2.10.3"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)
