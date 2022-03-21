#!/usr/bin/env bash

# Run on the Docker container specified in the args or by the user.
CONTAINER_ID=0
if [ $# -eq 0 ]
  then
    echo "Please specify a Docker container ID."
    docker ps
    read -r CONTAINER_ID
  else
    CONTAINER_ID=$1
fi

docker exec -ti $CONTAINER_ID /app/spark/bin/spark-submit --class upf.edu.TwitterWithWindow --master local[4] /app/dist/target/lab3-1.0-SNAPSHOT.jar /app/dist/target/classes/application.properties /app/dist/target/classes/map.tsv