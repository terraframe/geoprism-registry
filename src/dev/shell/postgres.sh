#!/bin/bash

# Run this with sudo

export POSTGRES_CONTAINER_NAME=cgr-postgres
export POSTGRES_PORT=5443
export POSTGRES_ROOT_PASS=postgres

# Exit immediately if anything errors out
set -ex

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=$POSTGRES_CONTAINER_NAME") > /dev/null || true

# Pull & Run the container
docker run --name $POSTGRES_CONTAINER_NAME -e POSTGRES_PASSWORD=$POSTGRES_ROOT_PASS -d -p $POSTGRES_PORT:5432 postgis/postgis:13-master
