#!/bin/sh

. ./set_variables.sh

GRADLE=/opt/gradle/gradle-$GRADLE_VERSION/bin/gradle

export JAVA_HOME=/opt/java/zulu$JAVA_VERSION

export MY_HOMES=$MY_HOME

sudo rm -rf tomcat_build

$GRADLE fullBuild