package com.playground.jobs

import org.apache.spark.sql.{Dataset, SparkSession}
import zio._

trait S3Writer {
  def write(outputBucket: String, data: Dataset[(Int, Int)]): Task[Unit]
}

object S3Writer {
  private class S3WriterImp(spark: SparkSession) extends S3Writer {

    override def write(outputBucket: String, data: Dataset[(Int, Int)]): Task[Unit] =
      ZIO.attempt(
        data.write
          .format("csv")
          .mode("overwrite")
          .save("s3a://bucket_name/path/to/output/")
      )
  }

  def live: ZLayer[SparkSession, Nothing, S3Writer] =
    ZLayer(ZIO.service[SparkSession].map(spark => new S3WriterImp(spark)))
}
