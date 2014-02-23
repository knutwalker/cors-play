licenses += "The MIT License" -> url("http://knutwalker.mit-license.org/license.txt")

scmInfo := Some(ScmInfo(url("https://github.com/knutwalker/cors-play"), "scm:git:https://github.com/knutwalker/cors-play.git", Some("scm:git:ssh://git@github.com:knutwalker/cors-play.git")))

pomExtra :=
  <developers>
    <developer>
      <id>knutwalker</id>
      <name>Paul Horn</name>
      <url>http://knutwalker.de/</url>
    </developer>
  </developers>

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomPostProcess := { (node: scala.xml.Node) =>
  val rewriteRule =
    new scala.xml.transform.RewriteRule {
      override def transform(n: scala.xml.Node): scala.xml.NodeSeq = {
        val name = n.nameToString(new StringBuilder).toString
          if (name == "dependency")
            if ((n \ "groupId").text == "com.sqality.scct")
              scala.xml.NodeSeq.Empty
            else n
          else n
      }
    }
  val transformer = new scala.xml.transform.RuleTransformer(rewriteRule)
  transformer.transform(node)(0)
}

