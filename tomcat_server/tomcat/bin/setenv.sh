#!/bin/bash
export MY_HOME=
export JPDA_ADDRESS="*:8001"
export JAVA_HOME="/usr/lib/jvm/java-1.17.0-openjdk-amd64"
export CATALINA_PID="/$MY_HOME/tomcat_build/temp/tomcat.pid"
export CATALINA_HOME="/$MY_HOME/tomcat_build"
export CATALINA_BASE="/$MY_HOME/tomcat_build"
export CATALINA_OPTS="$CATALINA_OPTS -Xms512M -Xmx1024M -server -XX:+UseParallelGC -Djava.awt.headless=true -Djava.security.egd=file:/dev/./urandom -Djdk.http.auth.tunneling.disabledSchemes= -Djdk.http.auth.proxying.disabledSchemes= -Duser.timezone=Asia/Kolkata"
