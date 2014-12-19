#!/bin/bash
rm target/scala-2.11/smulc.jar
sbt assembly
cat launcher.sh > target/smulc
cat target/scala-2.11/smulc.jar >> target/smulc