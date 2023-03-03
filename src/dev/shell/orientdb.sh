#!/bin/bash
#
#
#

# Run this with sudo
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

export ORIENTDB_CONTAINER_NAME=cgr-orientdb
export ORIENTDB_ROOT_PASS=root

# Exit immediately if anything errors out
set -ex

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=$ORIENTDB_CONTAINER_NAME") > /dev/null || true

# Pull & Run the orientdb container
docker run -d -p 2424:2424 -p 2480:2480 -e ORIENTDB_ROOT_PASSWORD=$ORIENTDB_ROOT_PASS -e ORIENTDB_OPTS_MEMORY="-Xms512M -Xmx2G" --name $ORIENTDB_CONTAINER_NAME orientdb:3.2
