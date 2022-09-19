#!/bin/bash
#
# Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
#
# This file is part of Geoprism Registry(tm).
#
# Geoprism Registry(tm) is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Geoprism Registry(tm) is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
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
