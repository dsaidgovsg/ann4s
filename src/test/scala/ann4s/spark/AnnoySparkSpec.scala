package ann4s.spark

import ann4s.Random
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession, SQLContext}
import org.scalatest.{FlatSpec, Matchers}

class AnnoySparkSpec extends FlatSpec with Matchers with LocalSparkContext {

  import ann4s.profiling.AnnoyDataset.{dataset => features, trueNns}

  object FixRandom extends Random {
    val rnd = new scala.util.Random(0)
    override def flip(): Boolean = rnd.nextBoolean()
    override def index(n: Int): Int = rnd.nextInt(n)
  }

  "Spark DataFrame-API" should "work" in {
    val sqlContext = new SQLContext(sc)

    val spark: SparkSession = sqlContext.sparkSession
    import spark.implicits._

    val idCol = "id"
    val featuresCol = "features"
    val neighborCol = "neighbor"
    val dimension = features.head.length

    val rdd: RDD[(Int, Array[Float])] =
      sc.parallelize(features.zipWithIndex.map(_.swap))

    val dataset: Dataset[_] = rdd.toDF(idCol, featuresCol).as[(Int, Array[Float])]

    val annoyModel: AnnoyModel = new Annoy()
      .setDimension(dimension)
      .setIdCol(idCol)
      .setFeaturesCol(featuresCol)
      .setNeighborCol(neighborCol)
      .setDebug(true)
      .fit(dataset)

    annoyModel
      .write
      .overwrite
      .save("annoy-spark-result")

    val loadedModel = AnnoyModel
      .read
      .context(sqlContext)
      .load("annoy-spark-result")

    val result: DataFrame = loadedModel
      .setK(10) // find 10 neighbors
      .transform(dataset)

    result.show()

    result.select(idCol, neighborCol)
      .map { case Row(id: Int, neighbor: Int) =>
        (id, neighbor)
      }
      .groupByKey(_._1)
      .mapGroups{ case (id, nns) => (id, nns.map(x => x._2).toArray) }
      .collect()
      .foreach { case (id, nns) =>
        nns.toSeq.intersect(trueNns(id)).length should be >= 2
      }
  }

}

