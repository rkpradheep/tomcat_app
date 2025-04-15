#!/bin/sh

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### JAVA SETUP FAILED #########${NC}"' EXIT


############## JAVA SETUP START ##############

if [ "$AUTO_MODE" = "false" ]; then
  echo "Do you want to setup JAVA? (yes/no)"
  read consent

  if ! [ "$consent" = "yes" ]; then
      echo "########## JAVA SETUP SKIPPED ##########"
      exit 0
  fi
fi

echo "############## Java setup started ##############\n"
sudo mkdir -p /opt/java
sudo chmod -R +777 /opt/java
sudo rm -rf /opt/java/zulu${JAVA_VERSION}

echo "############## Downloading java zulu 17.10 ##############\n"
wget -P /tmp https://cdn.azul.com/zulu/bin/zulu${JAVA_VERSION}.zip

echo "############## Extracting downloaded zip ##############\n"
sudo unzip -d /opt/java /tmp/zulu${JAVA_VERSION}.zip >/dev/null

echo "Add the below line to /etc/profile.d/setenv.sh to access it from anywhere"

echo "export PATH=/opt/java/zulu${JAVA_VERSION}:\$PATH\n"

sudo rm -rf /tmp/zulu${JAVA_VERSION}.zip

echo "${GREEN}############## Java setup completed ##############${NC}\n\n\n"

############## JAVA SETUP END ##############