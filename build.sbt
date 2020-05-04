name := "ann4s"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.5" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.4.5" % "provided",
  "org.scalatest" %% "scalatest" % "3.1.1" % "test"
)

organization := "com.github.mskimm"

licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")

