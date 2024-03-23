#!/bin/bash

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### OPERATION FAILED #########${NC}"' EXIT

echo "############## Build started ##############\n"


if [ -f ./custom/setenv.sh ]; then
    . ./custom/setenv.sh
fi

if git diff --quiet; then
  git pull origin master --rebase
fi

GRADLE=/opt/gradle/gradle-$GRADLE_VERSION/bin/gradle

export JAVA_HOME=/opt/java/zulu$JAVA_VERSION

export MY_HOME=$MY_HOME

sudo rm -rf tomcat_build

$GRADLE setUpServer

sudo systemctl restart tomcat

echo "${GREEN}############## Build completed ##############${NC}\n"
