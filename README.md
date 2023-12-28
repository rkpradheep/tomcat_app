Unix setup : 

To set environment variable MY_HOME, add below entry in /etc/profile

export MY_HOME=$HOME/MyHome



To allow sudoers to execute command without password, follow below steps

Step 1 : sudo visudo

Step 2 : Add this entry in last line -> %sudo   ALL=NOPASSWD:ALL

Step 3 : ctrl + x

Step 4 : y

Step 5 : Enter




To add a scheduler to monitor the server health for every one minute, follow the below steps

Step 1 : crontab -e

Step 2 : Add this entry in last line -> */1 * * * * . /etc/profile; /usr/local/bin/servermonitor >> $MY_HOME/output.txt 2>&1

Step 3 : ctrl + x

Step 4 : y

Step 5 : Enter


Add this under .bashrc

alias alog="tail -n 500 -f $MY_HOME/output.txt"
alias applog="tail  -n 500 -f $MY_HOME/tomcat_build/logs/my_app_log.txt"
alias clog="tail  -n 500 -f $MY_HOME/tomcat_build/logs/my_catalina_log.txt"


Docker commands:

sudo docker build -t tomcat-app .
sudo docker run -p 8090:8090 -p 8091:8091 -it tomcat-app:latest /bin/bash
