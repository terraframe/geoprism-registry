#!/bin/bash
#
#
#

# Run this with sudo
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

export CONTAINER_NAME=cgr-pgadmin
export PORT=5280
export PGADMIN_DEFAULT_EMAIL=admin@admin.com
export PGADMIN_DEFAULT_PASSWORD=admin
export ROOT_PASS=pgadmin

# Exit immediately if anything errors out
set -ex

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=$CONTAINER_NAME") > /dev/null || true

# Pull & Run the container
docker run --name $CONTAINER_NAME --network=host -e PGADMIN_LISTEN_PORT=$PORT -e PGADMIN_DEFAULT_EMAIL=$PGADMIN_DEFAULT_EMAIL -e PGADMIN_DEFAULT_PASSWORD=$PGADMIN_DEFAULT_PASSWORD -d dpage/pgadmin4

echo "The server should be running at http://localhost:$PORT. You can log in with $PGADMIN_DEFAULT_EMAIL / $PGADMIN_DEFAULT_PASSWORD"
