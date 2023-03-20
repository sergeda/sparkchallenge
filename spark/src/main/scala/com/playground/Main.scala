package com.playground

import com.playground.config.SparkAppConfig
import com.playground.jobs.{ChallengeRunner, S3Reader, S3Writer}
import zio.{config => _, _}

object Main extends ZIOAppDefault {

  def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    Application.app
      .provideSome[Scope with ZIOAppArgs](
        SparkAppConfig.live,
        SparkRuntime.live,
        Application.AwsProfileLayer,
        Application.argumentLayer,
        S3Writer.live,
        S3Reader.live,
        ChallengeRunner.live
      )

}
