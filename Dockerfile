FROM ubuntu:24.04

RUN apt-get update && \
    apt-get install -y git && \
    apt-get install -y wget && \
    apt-get install -y unzip && \
    apt-get install -y nano && \
    apt-get install -y net-tools && \
    apt-get install -y xz-utils && \
    apt-get clean

RUN apt-get install -y sudo

RUN apt-get install -y lsof

RUN apt-get install -y telnet

RUN apt-get install -y systemctl

ENV MY_HOME ${HOME}/MyHome

WORKDIR /MyHome

RUN git clone https://github.com/rkpradheep/tomcat_app.git .

RUN mkdir custom

RUN sh setup.sh auto

RUN ls