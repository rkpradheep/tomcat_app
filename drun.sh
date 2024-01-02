sudo docker build -t tomcat-app .

sudo docker rm --force tomcat_app

sudo docker run -d -t -p 8093:8093 -p 8092:8092 -p 8091:8091 --name tomcat_app tomcat-app:latest /bin/bash

sudo docker exec tomcat_app git pull --rebase

sudo docker cp ./app.properties tomcat_app:/MyHome/app.properties

sudo docker exec tomcat_app sed 's/db.server.ip = 127.0.0.1/db.server.ip = 172.21.117.19/' ./app.properties >> ./app.properties

sudo docker exec tomcat_app gradle fullBuild

sudo docker exec -it tomcat_app sh /MyHome/tomcat_build/run.sh