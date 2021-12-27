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


# This script is designed to run when your computer boots at the beginning of the day. It should
# launch all relevant programs necessary for development.
# This script should be idempotent, which should allow the script to be run in update contexts.
# This script has been tested on Ubuntu

# This script should be run as your home user (i.e. NOT sudo)

# Prerequisite software must be installed:
#  git, nvm, docker, mvn, java
# Additionally, your envcfg.properties must be created first before running this.

# Required environment variables must be set before running:
# CGR = /path/to/georegistry-git-checkout/../

######

CGR_PROJECT=$CGR/georegistry

# Exit on error
set -ex

# Update git
cd $CGR_PROJECT
git pull

cd $CGR/common-geo-registry-adapter/java
git pull
mvn install

# Run Docker containers
sudo docker start orientdb

# Kill any running tomcat
pkill -f -SIGINT catalina || true
sleep 2

# Run the ng2 server
cd $CGR_PROJECT/georegistry-web/src/main/ng2
gnome-terminal -x /bin/bash -c "source ~/.nvm/nvm.sh && nvm install lts/erbium && npm install && npm rebuild node-sass && npm run start"

# Run the cgr webserver
cd $CGR_PROJECT
mvn clean
gnome-terminal -x sh -c "mvn install -P ng2-dev,cargo-run-georegistry"

# Open a web browser to view the app
sleep 25
google-chrome https://localhost:8443/georegistry
