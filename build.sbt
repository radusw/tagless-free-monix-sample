import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "free-monix-sample",
    libraryDependencies ++= Seq (cats, monix, monixCats, specs2core % Test, specs2scalaCheck % Test)
  )
