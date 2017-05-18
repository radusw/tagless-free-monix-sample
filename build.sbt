import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "free-monix-sample",
    scalacOptions ++= Seq("-language:higherKinds"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++= Seq (cats, monix, monixCats, specs2core % Test, specs2scalaCheck % Test)
  )
