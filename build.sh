#!/bin/sh

. ./set_variables.sh

GRADLE=/opt/gradle/gradle-$GRADLE_VERSION/bin/gradle

export JAVA_HOME=/opt/java/zulu$JAVA_VERSION

sudo rm -rf tomcat_build

$GRADLE fullBuild