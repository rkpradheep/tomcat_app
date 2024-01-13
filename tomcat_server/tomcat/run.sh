#!/bin/bash

export JPDA_ADDRESS="8093"
export CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=Asia/Kolkata" # It will reflect here : ZoneId.systemDefault()
cd $MY_HOME/tomcat_build/bin
sh catalina.sh jpda run