# Use the official Ubuntu base image
FROM ubuntu:latest

# Install required dependencies
RUN apt-get update && \
    apt-get install -y git && \
    apt-get install -y openjdk-11-jdk && \
    apt-get install -y wget && \
    apt-get install -y unzip && \
    apt-get install -y nano && \
    apt-get install net-tools -y && \
    apt-get clean

# Install Gradle
ENV GRADLE_VERSION 6.1.1
RUN wget -P /tmp https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    rm -rf /tmp/*

ENV GRADLE_HOME /opt/gradle/gradle-${GRADLE_VERSION}
ENV PATH ${GRADLE_HOME}/bin:${PATH}
ENV MY_HOME ${HOME}/MyHome
ENV PATH ${MY_HOME}/bin:${PATH}

# Create a working directory
WORKDIR /MyHome

RUN git clone https://github.com/rkpradheep/tomcat_app.git .

COPY app.properties /MyHome/

RUN sed 's/db.server.ip = 127.0.0.1/db.server.ip = 172.21.117.19/' /MyHome/app.properties >> /MyHome/app.properties

RUN gradle fullBuild

