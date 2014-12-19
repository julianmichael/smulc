#!/bin/bash
rm target/scala-2.11/*.jar
sbt assembly
cat launcher.sh > target/smulc
cat target/scala-2.11/*.jar >> target/smulc