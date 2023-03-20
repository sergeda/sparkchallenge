package com.playground.jobs

import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import zio._

trait S3Reader {
  def read(pathToBucket: String): Task[Dataset[(Int, Int)]]
}

object S3Reader {
  val schema = StructType(
    List(StructField("index", IntegerType, false), StructField("value", IntegerType, false))
  )

  private class S3ReaderImp(spark: SparkSession) extends S3Reader {
    import spark.implicits._

    override def read(pathToBucket: String): Task[Dataset[(Int, Int)]] =
      ZIO.attempt {
        val df = spark.read
          .option("header", true)
          .schema(schema)
          .csv(pathToBucket + "/*.csv", pathToBucket + "/*.tsv")
        val dfWithZeros = df.na.fill(0)
        dfWithZeros.as[(Int, Int)]
      }

  }

  def live: ZLayer[SparkSession, Nothing, S3Reader] =
    ZLayer(ZIO.service[SparkSession].map(spark => new S3ReaderImp(spark)))
}
