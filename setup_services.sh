#!/bin/sh

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### SERVICE SETUP FAILED #########${NC}"' EXIT

sudo cp $MY_HOME/tomcat.service /etc/systemd/system
if test "$DB_SERVER" = "mysql" ; then
  sudo cp $MY_HOME/mysql.service /etc/systemd/system
  sudo sh -c "sed -i 's|mariadb.service|mysql.service|' /etc/systemd/system/tomcat.service"
else
  sudo cp $MY_HOME/mysql.service /etc/systemd/system
fi

sudo sh -c "sed -i 's|home_ph|${MY_HOME}|' /etc/systemd/system/tomcat.service"

if [ -z "$2" ] ; then
    sudo sh -c "sed -i 's|user_ph|root|' /etc/systemd/system/tomcat.service"
else
    sudo sh -c "sed -i 's|user_ph|$2|' /etc/systemd/system/tomcat.service"
fi

sudo systemctl daemon-reload

if test "$DB_SERVER" = "mysql"  ; then
  sudo systemctl start mysql
else
  sudo systemctl start mariadb
fi

echo "${GREEN}############## Service setup completed ##############${NC}\n\n\n"