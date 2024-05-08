docker build -t tomcat-app .

docker rm --force tomcat_app
#
docker run -d -t -p 8093:3128 -p 8091:443 -p 8090:80 -p 8092:8002 --name tomcat_app tomcat-app:latest /bin/bash

docker cp ./custom/remotecustom.properties tomcat_app:/MyHome/custom/custom.properties

#docker exec tomcat_app sed -i 's/db.server.ip = 127.0.0.1/db.server.ip = /' ./app.properties >> ./app.properties

docker exec tomcat_app sh build.sh

docker exec tomcat_app systemctl start mariadb
docker exec tomcat_app systemctl start tomcat

docker exec -it tomcat_app /bin/bash