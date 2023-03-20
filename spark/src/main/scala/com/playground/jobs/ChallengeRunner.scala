package com.playground.jobs

import org.apache.spark.HashPartitioner
import org.apache.spark.sql.{Dataset, SparkSession}
import zio._

trait ChallengeRunner {
  def solve(input: Dataset[(Int, Int)]): Task[Dataset[(Int, Int)]]
  def solveOptimized(input: Dataset[(Int, Int)]): Task[Dataset[(Int, Int)]]
}

object ChallengeRunner {
  class ChallengeRunnerImp(session: SparkSession) extends ChallengeRunner {
    import session.implicits._

    def solve(input: Dataset[(Int, Int)]): Task[Dataset[(Int, Int)]] =
      ZIO.attempt {
        val counts = input.repartition($"index").groupBy("index", "value").count()
        val oddCounts = counts.filter($"count" % 2 === 1)
        oddCounts.select("index", "value").as[(Int, Int)]
      }

    def solveOptimized(input: Dataset[(Int, Int)]): Task[Dataset[(Int, Int)]] =
      // Optimized version. Initially partition input data by index column
      // using HashPartitioner and process all data locally on executors
      ZIO.attempt {
        input.rdd
          .repartitionAndSortWithinPartitions(new HashPartitioner(200))
          .mapPartitions { iterator =>
            iterator
              .foldLeft[Option[(Int, Map[Int, Int])]](None) { case (accum, (index, value)) =>
                accum match {
                  case None => Some((index, Map(value -> 1)))
                  case Some((index, countMap)) =>
                    Some(
                      (
                        index,
                        countMap
                          .get(value)
                          .map(currentCount => countMap + (value -> (currentCount + 1)))
                          .getOrElse(countMap + (value -> 1))
                      )
                    )
                }
              }
              .map { case (index, countData) =>
                countData.toList.filter(_._2 % 2 != 0).map(_._1).map(v => (index, v)).iterator
              }
              .getOrElse(List.empty[(Int, Int)].iterator)
          }
          .toDF()
          .as[(Int, Int)]
      }
  }

  def live: ZLayer[SparkSession, Nothing, ChallengeRunnerImp] =
    ZLayer(ZIO.service[SparkSession].map(spark => new ChallengeRunnerImp(spark)))
}
