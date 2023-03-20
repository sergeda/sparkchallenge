package com.playground.config

import zio._
import zio.config._

import ConfigDescriptor._

case class SparkAppConfig(
    appName: String,
    master: String
)

object SparkAppConfig {
  private val sysEnvSource = ConfigSource.fromSystemEnv()
  private val config = (string("APP_NAME").default("SparkApp") zip
    string("MASTER_URL").default("local[*]")).map((SparkAppConfig.apply _).tupled)

  val live: ZLayer[Any, ReadError[String], SparkAppConfig] = ZLayer(read(config from sysEnvSource))
}
