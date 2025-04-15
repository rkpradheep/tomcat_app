#!/bin/sh

. ./set_variables.sh

set -e
trap '[ $? -eq 0 ] || echo "${RED}######### MYSQL SETUP FAILED #########${NC}"' EXIT


########### MYSQL SETUP START ##############

if [ "$AUTO_MODE" = "false" ]; then
  echo "Do you want to setup MYSQL? (yes/no)"
  read consent

  if ! [ "$consent" = "yes" ]; then
      echo "########## JAVA SETUP SKIPPED ##########"
      exit 0
  fi
fi

echo "############## Mysql setup started ##############\n"

if [ -f "/etc/systemd/system/mysql.service" ]; then
  sudo systemctl stop mysql
  sudo systemctl disable mysql
fi
if [ -f "/etc/systemd/system/mariadb.service" ]; then
  sudo systemctl stop mariadb
  sudo systemctl disable mariadb
fi

if ! $(grep -q mysql /etc/passwd) ; then
    echo "Adding mysql user"
    sudo useradd mysql
fi
sudo mkdir -p /opt/mysql
sudo rm -rf /opt/mysql/mysql-${MYSQL_VERSION}
sudo wget -P /tmp https://dev.mysql.com/get/Downloads/MySQL-8.3/mysql-${MYSQL_VERSION}.tar.xz
if ! [ -d "/opt/mysql" ]; then
  sudo mkdir /opt/mysql
fi
sudo chmod -R 777 /opt/mysql
sudo tar -xvf /tmp/mysql-${MYSQL_VERSION}.tar.xz --directory /opt/mysql
MYSQL_HOME=/opt/mysql/mysql-${MYSQL_VERSION}
sudo chmod -R +777 /opt/mysql/
sudo chown -R mysql:mysql $MYSQL_HOME
sudo cp mysql_server.sh $MYSQL_HOME
sudo chown mysql:mysql $MYSQL_HOME/mysql_server.sh
sudo apt-get install libaio1 libaio-dev libnuma-dev libncurses6
#workaround for libaio1 not availalbe in later version : curl -O http://launchpadlibrarian.net/646633572/libaio1_0.3.113-4_amd64.deb ; sudo dpkg -i libaio1_0.3.113-4_amd64.deb
#sudo apt-get install libaio-dev libnuma-dev libncurses6
cd $MYSQL_HOME
sudo touch my.cnf
sudo chmod -R 644 my.cnf
sudo sh -c "echo > my.cnf"
sudo sh -c "echo [client] >> my.cnf"
sudo sh -c "echo socket=${MYSQL_HOME}/data/mysql.sock >> my.cnf"
sudo sh -c "echo [mysqld] >> my.cnf"
sudo sh -c "echo server-id=2 >> my.cnf"
sudo sh -c "echo socket=${MYSQL_HOME}/data/mysql.sock >> my.cnf"
sudo sh -c "echo port=4000 >> my.cnf"
sudo sh -c "echo basedir=${MYSQL_HOME} >> my.cnf"
sudo sh -c "echo datadir=${MYSQL_HOME}/data >> my.cnf"
sudo rm -rf data
sudo mkdir data
sudo ./bin/mysqld --defaults-file=${MYSQL_HOME}/my.cnf --initialize  --user=mysql

sudo cp $MY_HOME/mysql_init_command.txt $MYSQL_HOME/data
sudo sh ./mysql_server.sh init

echo "Please enter the password as root if prompted to start and init the mysql"
sudo ./bin/mysql --defaults-file=$MYSQL_HOME/my.cnf -u root -p < $MY_HOME/dd-changes.sql

echo "Enter password below to stop the mysql for now"
sudo ./bin/mysqladmin --defaults-file=${MYSQL_HOME}/my.cnf --user=root -p shutdown

sudo rm -rf /tmp/mysql-${MYSQL_VERSION}.tar.xz

sudo chmod -R +777 /opt/mysql
sudo chmod -R 644 my.cnf

echo "${GREEN}############## Mysql setup completed ##############${NC}\n\n\n"

############# MYSQL SETUP END ##############