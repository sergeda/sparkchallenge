import Dependencies._
import Libraries._

Global / onChangedBuildSource := ReloadOnSourceChanges

initialize := {
  val _ = initialize.value // run the previous initialization
  val required = "11"
  val current  = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current[current version] != $required[required version]")
}

inThisBuild(
  List(
    organization := "com.playground",
    scalaVersion := "2.13.10",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
  )
)

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Ywarn-unused:imports"
  )


lazy val zioLoggingLibs= Seq(zioLogging, zioLoggingSlf4j, logBack)
lazy val zioConfigLibs = Seq(zioConfig)

lazy val zioLibs = Seq(zio) ++
  zioConfigLibs ++
  zioLoggingLibs

lazy val sparkLibs = Seq(sparkSql, hadoopAws, hadoopClient)
.map(_ % "provided")
lazy val otherLibs = Seq(awsSdk)

lazy val testLibs = Seq(zioTest, zioTestSbt).map(_ % Test)
lazy val itTestLibs = Seq(zioTest, zioTestSbt, testcontainers).map(_ % "it")

lazy val integrationSettings = Defaults.itSettings ++
  Seq(
    IntegrationTest / fork := true,
    IntegrationTest / parallelExecution := false
  )

lazy val `zio_spark` =
  Project(
    id = "zio_spark",
    base = file(".")
  ).disablePlugins(AssemblyPlugin)
    .settings(moduleName := "root")
    .aggregate(spark)

lazy val spark =
  project.in(file("spark"))
    .configs(IntegrationTest)
    .settings(integrationSettings)
    .settings(Seq(
      resolvers ++= Seq("public", "snapshots", "releases").flatMap(Resolver.sonatypeOssRepos),
      version := sys.env.getOrElse("RELEASE_VERSION", "0.0.1"),
      assembly / assemblyJarName := s"spark.jar",
      assembly / mainClass := Some("com.playground.Main"),
      assembly / assemblyOption ~= {
        _.withIncludeScala(true)
      },
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
      libraryDependencies ++= sparkLibs ++ zioLibs ++ testLibs ++ otherLibs ++ itTestLibs,
      Test / fork := true,
    ))
    .enablePlugins(JavaAppPackaging)


addCommandAlias("sanity", "clean;compile;scalafixAll;scalafmtAll;test")
