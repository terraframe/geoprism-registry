#!/bin/bash

# Run this with sudo

export ORIENTDB_CONTAINER_NAME=orientdb
export ORIENTDB_ROOT_PASS=root

# Exit immediately if anything errors out
set -ex

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=orientdb") > /dev/null || true

# Pull & Run the orientdb container
docker run -d -p 2424:2424 -p 2480:2480 -e ORIENTDB_ROOT_PASSWORD=$ORIENTDB_ROOT_PASS --name $ORIENTDB_CONTAINER_NAME orientdb:3.0
