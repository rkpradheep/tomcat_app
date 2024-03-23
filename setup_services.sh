#!/bin/sh

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### SERVICE SETUP FAILED #########${NC}"' EXIT

sudo cp $MY_HOME/tomcat.service /etc/systemd/system
if test "$DB_SERVER" = "mysql" ; then
  sudo cp $MY_HOME/mysql.service /etc/systemd/system
  sudo sh -c "sed -i 's|mariadb.service|mysql.service|' /etc/systemd/system/tomcat.service"
else
  sudo cp $MY_HOME/mariadb.service /etc/systemd/system
fi

sudo sh -c "sed -i 's|home_ph|${MY_HOME}|' /etc/systemd/system/tomcat.service"

if [ -z "$CUSTOM_TOMCAT_USER"] ; then
    sudo sh -c "sed -i 's|user_ph|root|' /etc/systemd/system/tomcat.service"
else
    sudo sh -c "sed -i 's|user_ph|$CUSTOM_TOMCAT_USER|' /etc/systemd/system/tomcat.service"
fi

sudo systemctl daemon-reload


echo "${GREEN}############## Service setup completed ##############${NC}\n\n\n"