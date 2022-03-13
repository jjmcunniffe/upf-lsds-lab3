FROM ubuntu:latest

# Update and install our packages.
RUN apt-get update -y && apt-get upgrade -y
RUN apt-get install -y openjdk-8-jdk

# Create our application directory.
RUN mkdir /app
WORKDIR /app

# Fetch and install Spark and Hadoop.
ADD https://archive.apache.org/dist/spark/spark-2.4.4/spark-2.4.4-bin-hadoop2.6.tgz .
RUN tar -zxvf spark-2.4.4-bin-hadoop2.6.tgz
RUN mv spark-2.4.4-bin-hadoop2.6/ /app/spark
