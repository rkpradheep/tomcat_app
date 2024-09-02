#!/bin/sh

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### DB REINIT FAILED #########${NC}"' EXIT

MARIADB_HOME=/opt/mariadb/mariadb-${MARIADB_VERSION}
cd $MARIADB_HOME

sudo ./bin/mariadb --defaults-file=$MARIADB_HOME/my.cnf -u root -proot -e "DROP DATABASE IF EXISTS tomcatserver;"
sudo ./bin/mariadb --defaults-file=$MARIADB_HOME/my.cnf -u root -proot < $MY_HOME/dd-changes.sql

echo "${GREEN}############## Reinit Success ##############${NC}\n\n\n"