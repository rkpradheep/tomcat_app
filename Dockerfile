FROM ubuntu:latest

RUN apt-get update && \
    apt-get install -y git && \
    apt-get install -y openjdk-11-jdk && \
    apt-get install -y wget && \
    apt-get install -y unzip && \
    apt-get install -y nano && \
    apt-get install net-tools -y && \
    apt-get clean

ENV GRADLE_VERSION 6.1.1
RUN wget -P /tmp https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    rm -rf /tmp/*

ENV GRADLE_HOME /opt/gradle/gradle-${GRADLE_VERSION}
ENV PATH ${GRADLE_HOME}/bin:${PATH}
ENV MY_HOME ${HOME}/MyHome
ENV PATH ${MY_HOME}/bin:${PATH}

WORKDIR /MyHome

RUN git clone https://github.com/rkpradheep/tomcat_app.git .