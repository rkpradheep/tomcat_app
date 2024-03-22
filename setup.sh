#!/bin/sh

. ./set_variables.sh

############## GRADLE SETUP START ##############

sudo rm -rf /opt/gradle/gradle-${GRADLE_VERSION}

echo "############## Downloading Gradle ${GRADLE_VERSION} ##############"
sudo wget -P /tmp https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip

echo "############## Extracting downloaded zip ##############"
sudo unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip >/dev/null

GRADLE_HOME=/opt/gra0dle/gradle-${GRADLE_VERSION}/bin

############## GRADLE SETUP END ##############



############## JAVA SETUP START ##############

sudo rm -rf /opt/java/zulu${JAVA_VERSION}

echo "############## Downloading java zulu 17.10 ##############"
wget -P /tmp https://cdn.azul.com/zulu/bin/zulu${JAVA_VERSION}.zip

echo "############## Extracting downloaded zip ##############"
sudo unzip -d /opt/java /tmp/zulu${JAVA_VERSION}.zip >/dev/null

JAVA_HOME=/opt/java/zulu${JAVA_VERSION}/bin

############## JAVA SETUP END ##############



sudo rm -rf /tmp/*

echo 'Add the below line in /etc/profile.d/setenv.sh'
echo "export PATH=${JAVA_HOME}:${GRADLE_HOME}"':$PATH'
