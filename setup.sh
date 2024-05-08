#!/bin/sh

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### SETUP FAILED #########${NC}"' EXIT

export AUTO_MODE="false"
if [ "$1" = "auto" ]; then
  export AUTO_MODE="true"
fi

sh setup_gradle.sh

sh setup_java.sh

if test "$DB_SERVER" = "mysql" ; then
  sh setup_mysql.sh
else
  # Using repository
  #  sudo apt-get purge mariadb-server
  #  sudo apt-get autoremove
  #  sudo apt-get install mariadb-server
  #  echo "Enter the password as root if prompted"
  #  sudo mariadb -u root -p -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';"
  #  echo "Enter the password as root if prompted to initialize tables and default values"
  #  sudo mariadb -u root -p < $MY_HOME/dd-changes.sql
  sh setup_mariadb.sh
fi

sh setup_services.sh

sh build.sh