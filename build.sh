#!/bin/bash

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### OPERATION FAILED #########${NC}"' EXIT

echo "############## Build started ##############\n"

#if [ -z "$(git log origin/$(git rev-parse --abbrev-ref HEAD)..HEAD)" ]; then
#    if git diff --quiet; then
#      git pull origin master --rebase
#    fi
#else
#  echo "${RED}############## There are some unpushed commits. Please push and try again ##############${NC}\n"
#  exit 1
#fi

GRADLE=/opt/gradle/gradle-$GRADLE_VERSION/bin/gradle

export JAVA_HOME=/opt/java/zulu$JAVA_VERSION

export MY_HOME=$MY_HOME

sudo systemctl stop tomcat

sudo rm -rf tomcat_build

$GRADLE setUpServer

sudo systemctl start tomcat

echo "${GREEN}############## Build completed ##############${NC}\n"
