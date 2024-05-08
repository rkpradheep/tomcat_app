#!/bin/sh

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### TOMCAT UPDATE FAILED #########${NC}"' EXIT


############## TOMCAT UPDATE START ##############


echo "############## Tomcat update started ##############\n"

echo "############## Downloading tomcat  ##############\n"

wget -P /tmp https://dlcdn.apache.org/tomcat/tomcat-$(echo $TOMCAT_VERSION | awk -F'.' '{print $1}')/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz

if ! [ -d "/opt/tomcat" ]; then
  sudo mkdir /opt/tomcat
fi

sudo tar -xvf /tmp/apache-tomcat-${TOMCAT_VERSION}.tar.gz --directory /opt/tomcat

(cd /opt/tomcat/apache-tomcat-${TOMCAT_VERSION} && sudo zip -r $MY_HOME/tomcat_binary.zip .)

sudo chmod 777 tomcat_binary.zip
sudo rm -rf /tmp/apache-tomcat-${TOMCAT_VERSION}.tar.gz
sudo rm -rf /opt/tomcat/apache-tomcat-${TOMCAT_VERSION}

echo "${GREEN}############## Tomcat update completed ##############${NC}\n\n\n"

############## TOMCAT UPDATE END ##############