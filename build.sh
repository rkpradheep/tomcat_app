#!/bin/bash

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### OPERATION FAILED #########${NC}"' EXIT

echo "############## Build started ##############\n"


if [ -z "$(git log origin/$(git rev-parse --abbrev-ref HEAD)..HEAD)" ]; then
    if git diff --quiet; then
      git pull origin master --rebase
    fi
else
  echo "${RED}############## There are some unpushed commits. Please push and try again ##############${NC}\n"
  exit 1
fi

GRADLE=/opt/gradle/gradle-$GRADLE_VERSION/bin/gradle

export JAVA_HOME=/opt/java/zulu$JAVA_VERSION

export MY_HOME=$MY_HOME

if [ "$1" != "auto" ]; then
  sudo systemctl stop tomcat
fi


sudo rm -rf tomcat_build

echo "JAVA_HOME : ${JAVA_HOME}"
echo "GRADLE : ${GRADLE}"
echo "MY_HOME : ${MY_HOME}"

$GRADLE setUpServer

if [ "$1" != "auto" ]; then
  sudo systemctl start tomcat
fi

echo "${GREEN}############## Build completed ##############${NC}\n"
