package com.playground.jobs
import scala.jdk.CollectionConverters._

import com.playground.{AWSProfile, SparkRuntime}
import com.playground.config.SparkAppConfig
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import zio._
import zio.test._
import zio.test.Assertion._

object ChallengeRunnerSpec extends ZIOSpecDefault {
  def spec = suite("ChallengeRunner should")(
    test("return numbers that occuried odd number of times") {
      val expect = List((1, 3), (2, 3))
      for {
        spark <- ZIO.service[SparkSession]
        df <- createTestDataset(
          spark,
          List((1, 2), (1, 3), (1, 2), (2, 1), (2, 3), (2, 1))
        )
        df <- ZIO.serviceWithZIO[ChallengeRunner](_.solve(df))
        df2 <- ZIO.serviceWithZIO[ChallengeRunner](_.solveOptimized(df))
        result <- ZIO.attempt(df.collectAsList().asScala)
        result2 <- ZIO.attempt(df2.collectAsList().asScala)
      } yield assert(result)(hasSameElementsDistinct(expect)) &&
        assert(result2)(hasSameElementsDistinct(expect))
    }
  ).provideShared(
    Scope.default,
    ChallengeRunner.live,
    SparkRuntime.live,
    SparkAppConfig.live,
    ZLayer.succeed[Option[AWSProfile]](None)
  )

  private def createTestDataset(session: SparkSession, data: List[(Int, Int)]): Task[Dataset[(Int, Int)]] = {
    import session.implicits._
    ZIO.attempt {
      val rdd: RDD[Row] = session.sparkContext.parallelize(data.map(Row.fromTuple(_)))
      session.createDataFrame(rdd, S3Reader.schema).as[(Int, Int)]
    }
  }
}
