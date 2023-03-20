import sbt._

object Dependencies {

  object Versions {
    val zioV = "2.0.10"
    val zioConfigV = "3.0.7"
    val zioLoggingV = "2.1.11"
    val logBackV = "1.1.2"
    val softwareAwsSdkV = "2.17.202"
    val slf4jV = "1.7.36"
    val testcontainersV = "0.40.12"
    val sparkV = "3.3.2"
    val hadoopV = "3.3.4"
  }

  object Libraries {

    import Versions._

    val zio               = "dev.zio" %% "zio"                          % zioV
    val zioConfig         = "dev.zio" %% "zio-config"                   % zioConfigV
    val zioTest           = "dev.zio" %% "zio-test"                     % zioV
    val zioTestSbt        = "dev.zio" %% "zio-test-sbt"                 % zioV
    val zioLogging        = "dev.zio" %% "zio-logging"                  % zioLoggingV
    val zioLoggingSlf4j   = "dev.zio" %% "zio-logging-slf4j"            % zioLoggingV


    val logBack         = "ch.qos.logback"  %   "logback-classic"           % logBackV
    val testcontainers  = "com.dimafeng"    %%  "testcontainers-scala-core" % testcontainersV

    val sparkSql = ("org.apache.spark" %% "spark-sql" % sparkV)
      .exclude("org.apache.logging.log4j", "log4j-slf4j-impl")
      .excludeAll("org.apache.hadoop")

    val hadoopAws = ("org.apache.hadoop" % "hadoop-aws" % hadoopV)
      .exclude("org.apache.logging.log4j", "log4j-slf4j-impl")

    val hadoopClient =  ("org.apache.hadoop" % "hadoop-client" % hadoopV)
      .exclude("org.apache.logging.log4j", "log4j-slf4j-impl")

    val awsSdk = "software.amazon.awssdk" % "bundle" % "2.17.257"
  }
}
