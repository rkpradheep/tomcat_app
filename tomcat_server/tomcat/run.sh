#!/bin/bash

export JPDA_ADDRESS="8093"
export CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=Asia/Kolkata -Xmx1024m -Xms1024m -XX:+HeapDumpOnOutOfMemoryError -XX:PermSize=256M -Djdk.http.auth.tunneling.disabledSchemes= -Djdk.http.auth.proxying.disabledSchemes= " # It will reflect here : ZoneId.systemDefault()
cd $MY_HOME/tomcat_build/bin

COMMAND="run"

if [ ! -z "$1" ] ; then
  COMMAND="$1"
fi

if [ "$COMMAND" == "stop" ] ; then
  sh catalina.sh stop
else
  sh catalina.sh jpda $COMMAND
fi