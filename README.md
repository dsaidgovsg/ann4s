[![Build Status](https://travis-ci.org/mskimm/ann4s.svg?branch=master)](https://travis-ci.org/mskimm/ann4s)

# Ann4s

This is a fork of the original [`mskimm/ann4s`](https://github.com/mskimm/ann4s), and is based off
tag `v0.0.6`. The rest of the `README.md` is generally similar to the original but with some details
amended.

A Scala Implementation of [Annoy](https://github.com/spotify/annoy) which searches nearest neighbors
given query point.

Ann4s also provides [DataFrame-based API](http://spark.apache.org/docs/latest/ml-guide.html)
for [Apache Spark](https://spark.apache.org/).

# Scala code example

```scala
import ann4s._

object AnnoyExample {

  def main(args: Array[String]) {
    val f = 40
    val metric: Metric = Angular // or Euclidean
    val t = new AnnoyIndex(f, metric)  // Length of item vector that will be indexed
    (0 until 1000) foreach { i =>
      val v = Array.fill(f)(scala.util.Random.nextGaussian().toFloat)
      t.addItem(i, v)
    }
    t.build(10)

    // t.getNnsByItem(0, 1000) runs using HeapByteBuffer (memory)

    t.save("test.ann") // `test.ann` is compatible with the native Annoy

    // after `save` t.getNnsByItem(0, 1000) runs using MappedFile (file-based)

    println(t.getNnsByItem(0, 1000).mkString(",")) // will find the 1000 nearest neighbors
  }

}

```

# Spark code example (with DataFrame-based API)

## Item similarity computation

```scala
val dataset: DataFrame = ??? // your dataset

val alsModel: ALSModel = new ALS()
  .fit(dataset)

val annoyModel: AnnoyModel = new Annoy()
  .setDimension(alsModel.rank)
  .fit(alsModel.itemFactors)

val result: DataFrame = annoyModel
  .setK(10) // find 10 neighbors
  .transform(alsModel.itemFactors)

result.show()
```

The `result.show()` shows

```markdown
+---+--------+-----------+
| id|neighbor|   distance|
+---+--------+-----------+
|  0|       0|        0.0|
|  0|      50|0.014339785|
...
|  1|       1|        0.0|
|  1|      36|0.011467933|
...
+---+--------+-----------+
```

- For more information of ALS see this [link](http://spark.apache.org/docs/2.0.0/ml-collaborative-filtering.html)
- Working example is at 'src/test/scala/ann4s/spark/AnnoySparkSpec.scala'

# Installation (not applicable for this fork version)

```scala
resolvers += Resolver.bintrayRepo("mskimm", "maven")

libraryDependencies += "com.github.mskimm" %% "ann4s" % "0.0.6"
```

- `0.0.6` is built with Apache Spark 2.4.5

# Forked version installation

To use this forked version in another `sbt` project, put these at the root nesting level in the
project `build.sbt`:

```scala
lazy val root = (project in file(".")).dependsOn(ann4sLib)
lazy val ann4sLib = RootProject(uri("git://github.com/dsaidgovsg/ann4s.git#v0.0.6_spark-2.4.5_scala-2.12"))
```

The package is not published in Maven in anyway, so this is the only possible way to "use" the
dependency. Note that Scala 2.11 is also supported at the moment, but the primary focus is to
support Scala 2.12.

# References

- <https://github.com/spotify/annoy> : native implementation with serveral bindings like Python
- <https://github.com/pishen/annoy4s> : Scala wrapper using JNA
- <https://github.com/spotify/annoy-java> : Java implementation
