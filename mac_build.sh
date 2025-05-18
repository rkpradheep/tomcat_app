#!/bin/bash

. ./set_variables.sh

source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk use gradle 7.3

sdk use java 17.0.14-zulu

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

export MY_HOME=$MY_HOME

setupMysql() {

MACHINE_NAME="podman-machine-default"
podman=/opt/podman/bin/podman

  if ! $podman machine inspect "$MACHINE_NAME" | grep -q '"State": "running"'; then
      echo "Starting Podman machine '$MACHINE_NAME'..."
      $podman machine start "$MACHINE_NAME"
  else
      echo "Podman machine '$MACHINE_NAME' is already running."
  fi

    CONTAINER_NAME="mysql-tomcat-container"

    if $podman ps --filter "name=^${CONTAINER_NAME}$" --filter "status=running" --format "{{.Names}}" | grep -wq "$CONTAINER_NAME"; then
        echo "Container '$CONTAINER_NAME' is already running."
    else
        echo "Container '$CONTAINER_NAME' is not running."

        if $podman ps -a --filter "name=^${CONTAINER_NAME}$" --format "{{.Names}}" | grep -wq "$CONTAINER_NAME"; then
            echo "Starting existing container '$CONTAINER_NAME'..."
            $podman start "$CONTAINER_NAME"
        else
            echo "Creating and running new container '$CONTAINER_NAME'..."
            $podman run -d --name "$CONTAINER_NAME" -p 4000:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=yes mysqldev:8.0.35
        fi
    fi
}

rm -rf tomcat_build

setupMysql
gradle setUpServer

sh $MY_HOME/tomcat_build/run.sh


echo "${GREEN}############## Build completed ##############${NC}\n"
