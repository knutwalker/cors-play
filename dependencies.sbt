resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  object Version {
    val Play = "2.2.1"
    val Scalatest  = "2.0"
    val Scalacheck = "1.10.1"
  }
  Seq(
    "com.typesafe.play" %% "play"       % Version.Play       % "provided",
    "org.scalatest"     %% "scalatest"  % Version.Scalatest  % "test",
    "org.scalacheck"    %% "scalacheck" % Version.Scalacheck % "test"
  )
}
