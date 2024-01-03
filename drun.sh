docker build -t tomcat-app .

docker rm --force tomcat_app

docker run -d -t -p 8093:8093 -p 8092:8092 -p 8091:8091 -p 8090:8090 --name tomcat_app tomcat-app:latest /bin/bash

docker exec tomcat_app git pull --rebase

docker cp ./app.properties tomcat_app:/MyHome/app.properties

docker exec tomcat_app sed -i 's/db.server.ip = 127.0.0.1/db.server.ip = 172.21.117.19/' ./app.properties >> ./app.properties

docker exec tomcat_app gradle fullBuild

docker exec -it tomcat_app sh /MyHome/tomcat_build/run.sh