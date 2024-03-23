6 Steps to set up build in linux distributions:

1.) Create MyHome directory at your desired location using this command (mkdir MyHome)

2.) Step into the MyHome directory using this command (cd MyHome)

3.) Clone the server repository using this command (git clone https://github.com/rkpradheep/tomcat_app.git .)

4.) Once the repository is cloned, enter this command to start the setup (sh setup.sh)

5.) Once the setup script is run successfully, enter this command (sh build.sh)

6.) Once the build is successful, type the following commands to install the server as service so that it will be started automatically post OS boot

        * sudo systemctl daemon-reload

        * sudo systemctl start tomcat

        * sudo systemctl enable tomcat

        * sudo systemctl start mysql

        * sudo systemctl enable mysql



Custom configuration steps

1.) For custom properties and environment variables, create a new directory named "custom" inside MyHome directory.


2.) All the custom environment variables can be set in setenv.sh file under custom directory.

Example (MyHome/custom/setenv.sh) :

#!/bin/sh
export CUSTOM_KEYSTORE_FILE="demo.keystore"
export CUSTOM_KEYSTORE_PASSWORD="demo"
export TOMCAT_CUSTOM_HTTP_PORT="8080"
export TOMCAT_CUSTOM_HTTPS_PORT="8443"
export DB_SERVER="mysql"




3.) All the custom properties can ve set in custom.properties file under custom directory.

Example (MyHome/custom/custom.properties) : 

mail.user = abc@gmail.com
mail.password = demo