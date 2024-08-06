#!/bin/sh


. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### GRADLE SETUP FAILED #########${NC}"' EXIT

############## GRADLE SETUP START ##############

if [ "$AUTO_MODE" = "false" ]; then
  echo "Do you want to setup GRADLE? (yes/no)"
  read consent

  if ! [ "$consent" = "yes" ]; then
      echo "########## GRADLE SETUP SKIPPED ##########"
      exit 0
  fi
fi


echo "############## Gradle setup started ##############\n"

sudo rm -rf /opt/gradle/gradle-${GRADLE_VERSION}
sudo chmod -R +777 /opt/gradle
echo "############## Downloading Gradle ${GRADLE_VERSION} ##############\n"
sudo wget -P /tmp https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip

echo "############## Extracting downloaded zip ##############\n"
sudo unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip >/dev/null

GRADLE_HOME=/opt/gra0dle/gradle-${GRADLE_VERSION}/bin

sudo rm -rf /tmp/gradle-${GRADLE_VERSION}-bin.zip

echo "${GREEN}############## Gradle setup completed ##############${NC}\n\n\n"

############## GRADLE SETUP END ##############
