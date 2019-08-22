FROM jenkins/jenkins:lts

USER root 

RUN apt-get update

RUN curl -fsSL https://get.docker.com | sh

RUN docker --version 

RUN apt-get install jq -y

USER root