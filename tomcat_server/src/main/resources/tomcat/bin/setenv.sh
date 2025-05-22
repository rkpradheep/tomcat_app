#!/bin/sh
export MY_HOME=
export CUSTOM_JAVA_OPTS=
export JPDA_ADDRESS="*:8002"
export JAVA_HOME=
export CATALINA_PID="/$MY_HOME/tomcat_build/temp/tomcat.pid"
export CATALINA_HOME="$MY_HOME/tomcat_build"
export CATALINA_BASE="$MY_HOME/tomcat_build"
CP="$CATALINA_BASE/lib/javassist-3.30.2.jar"
export CLASSPATH="$CLASSPATH":$CP
export CATALINA_OPTS="$CATALINA_OPTS -Xms256m -Xmx512m -XX:+UseSerialGC -Djava.protocol.handler.pkgs=com.server.system  --add-exports java.base/sun.net.www.protocol.http=ALL-UNNAMED  --add-exports java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-exports java.base/sun.net.www.http=ALL-UNNAMED -Djava.security.egd=file:/dev/./urandom -Djdk.http.auth.tunneling.disabledSchemes= -Djdk.http.auth.proxying.disabledSchemes= -Duser.timezone=Asia/Kolkata -javaagent:$MY_HOME/tomcat_build/lib/instrumentation.jar $CUSTOM_JAVA_OPTS"