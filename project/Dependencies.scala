import sbt._

object Dependencies {
  lazy val cats = "org.typelevel" %% "cats" % "0.9.0"
  lazy val monix = "io.monix" %% "monix" % "2.3.0"
  lazy val monixCats = "io.monix" %% "monix-cats" % "2.3.0"
  lazy val specs2core = "org.specs2" %% "specs2-core" % "3.8.9"
  lazy val specs2scalaCheck = "org.specs2" %% "specs2-scalacheck" % "3.8.9"
}
