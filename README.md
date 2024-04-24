5 Steps to set up build in linux distributions:

1.) Create MyHome directory at your desired location using this command (mkdir MyHome)

2.) Step into the MyHome directory using this command (cd MyHome)

3.) Clone the server repository using this command (git clone https://github.com/rkpradheep/tomcat_app.git .)

4.) Once the repository is cloned, enter this command to start the setup (sh setup.sh)

5.) Once the setup and build is successful, type the following commands to install the server as service so that it will be started automatically post OS boot

        * sudo systemctl enable tomcat

        * sudo systemctl start mysql

        * sudo systemctl enable mysql

<br /><br />

Custom configuration steps

For custom properties and custom file, create a new directory named "custom" inside MyHome directory.

All the custom properties can be set in custom.properties file under custom directory.

Example (MyHome/custom/custom.properties) : 

mail.user = abc@gmail.com

mail.password = demo

custom.keystore.file = demo.keystore

custom.keystore.password = demo

custom.tomcat.http.port = 8080

custom.tomcat.https.port = 8443