package com.playground

import org.apache.spark.sql.SparkSession
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import zio.{Scope, ZIO, ZLayer}

import config.SparkAppConfig

case class AWSProfile(value: String)

object SparkRuntime {

  def sparkScoped(
      config: SparkAppConfig,
      awsProfile: Option[AWSProfile]
  ): ZIO[Scope, Throwable, SparkSession] = {
    val builder = SparkSession.builder()
    for {
      modifiedBuilder <- buildCredentialProvider(awsProfile, builder)
      session <- ZIO.acquireRelease(
        ZIO.attemptBlocking {
          modifiedBuilder
            .appName(config.appName)
            .master(config.master)
            .getOrCreate()
        }
      )(spark =>
        ZIO.attemptBlocking {
          spark.stop()
        }.ignoreLogged
      )
    } yield session
  }

  private def buildCredentialProvider(awsProfile: Option[AWSProfile], sessionBuilder: SparkSession.Builder) =
    ZIO.attempt {
      awsProfile
        .map { profile =>
          val credentials =
            ProfileCredentialsProvider.builder().profileName(profile.value).build().resolveCredentials()
          sessionBuilder
            .config("spark.hadoop.fs.s3a.access.key", credentials.accessKeyId())
            .config("spark.hadoop.fs.s3a.secret.key", credentials.secretAccessKey())
            .config("spark.hadoop.fs.s3a.endpoint", "s3.amazonaws.com")
        }
        .getOrElse(
          sessionBuilder
        )
    }

  def live: ZLayer[Scope with SparkAppConfig with Option[AWSProfile], Throwable, SparkSession] =
    ZLayer.fromZIO(
      for {
        config <- ZIO.service[SparkAppConfig]
        awsProfile <- ZIO.service[Option[AWSProfile]]
        spark <- sparkScoped(config, awsProfile)
      } yield spark
    )

}
