import sbt._
import Keys._

object MyBuild extends Build {
  lazy val smulc = Project("smulc", file(".")).
    dependsOn(molt % "compile;test;test->test")
  lazy val molt = RootProject( file("../molt") )
}