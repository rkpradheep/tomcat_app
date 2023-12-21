#!/bin/bash

export JPDA_ADDRESS=8002
#export JAVA_OPTS="-Djava.awt.headless=true -Xms512m -Xmx2048m"
cd $MY_HOME/tomcat_build/bin
sh catalina.sh jpda run