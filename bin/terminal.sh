#!/usr/bin/env bash

# Run on the Docker container specified in the args or by the user.
CONTAINER_ID=0
if [ $# -eq 0 ]
  then
    echo "Please specify a Docker container ID."
    read -r CONTAINER_ID
  else
    CONTAINER_ID=$1
fi

docker exec -ti $CONTAINER_ID /bin/bash