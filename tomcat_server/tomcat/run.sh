#!/bin/bash
sed -i 's/8001/8093/' ./bin/setenv.sh >> ./bin/setenv.sh
sed -i 's/export JAVA_HOME/#export JAVA_HOME/' ./bin/setenv.sh >> ./bin/setenv.sh


sed -i 's/port="80"/port="8090"/' ./conf/server.xml >>  ./conf/server.xml
sed -i 's/port="443"/port="8091"/' ./conf/server.xml >>  ./conf/server.xml


sed -i 's/tomcat.jks/sas.keystore/' ./conf/server.xml >>  ./conf/server.xml
sed -i 's/keystorePass="tomcat"/keystorePass="Uwqxh9h11"/' ./conf/server.xml >>  ./conf/server.xml

sh ./bin/catalina.sh jpda run