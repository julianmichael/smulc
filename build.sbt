name := "smulc"

version := "0.8"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
  "junit" % "junit" % "4.10" % "test")

mainClass := Some("smulc.Main")

mainClass in assembly := Some("smulc.Main")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(prependShellScript =
  Some(defaultShellScript))

assemblyJarName in assembly := s"${name.value}-${version.value}"

