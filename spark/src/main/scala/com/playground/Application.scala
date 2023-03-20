package com.playground

import com.playground.jobs.{ChallengeRunner, S3Reader, S3Writer}
import zio._

import input.InputArguments

object Application {

  sealed trait ApplicationError extends Exception {
    val message: String
    val maybeCause: Option[Throwable]

    override def getMessage: String = message

    override def getCause: Throwable = maybeCause.orNull
  }
  case object IncorrectNumberOfArguments extends ApplicationError {
    val message: String =
      "Incorrect number of arguments. You should provide next arguments: input bucket, output bucket, AWS profile (optional)"
    val maybeCause: Option[Throwable] = None
  }
  case object IncorrectPositionOfArguments extends ApplicationError {
    val message: String =
      "Incorrect position of arguments. You should provide arguments in this order: input bucket, output bucket, AWS profile (optional)"
    val maybeCause: Option[Throwable] = None
  }

  val argumentLayer: ZLayer[ZIOAppArgs, ApplicationError, InputArguments] =
    ZLayer(for {
      args <- ZIO.serviceWith[ZIOAppArgs](_.getArgs)
      _ <- ZIO.fail(IncorrectNumberOfArguments).unless(args.length == 3 || args.length == 2)
      _ <- ZIO
        .fail(IncorrectPositionOfArguments)
        .unless(args.head.startsWith("http") && args(1).startsWith("http"))
    } yield InputArguments(args))

  val AwsProfileLayer = argumentLayer.project(_.awsProfile)

  val app: ZIO[InputArguments with S3Reader with ChallengeRunner with S3Writer, Throwable, Unit] =
    (for {
      inputArguments <- ZIO.service[InputArguments]
      _ <- ZIO.logInfo(inputArguments.toString())
      s3Reader <- ZIO.service[S3Reader]
      inputData <- s3Reader.read(inputArguments.inputS3path)
      runner <- ZIO.service[ChallengeRunner]
      result <- runner.solveOptimized(inputData)
      s3Writer <- ZIO.service[S3Writer]
      _ <- s3Writer.write(inputArguments.outputS3Path, result)
    } yield ())
}
