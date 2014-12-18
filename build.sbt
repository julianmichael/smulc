name := "smulc"

version := "0.1"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
  "junit" % "junit" % "4.10" % "test")

mainClass := Some("deduction.TestMain")
