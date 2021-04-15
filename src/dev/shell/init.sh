#!/bin/bash

# This script is designed to be used by developers when setting up / updating a new environment.
# This script should be idempotent, which should allow the script to be run in update contexts.
# This script has been tested on Ubuntu

# This script should be run as your home user (i.e. NOT sudo)

# Prerequisite software must be installed:
#  git, nvm, docker, mvn, java
# Additionally, your envcfg.properties must be created first before running this.

# Required environment variables must be set before running:
# (NONE)

######

BASEDIR=$(dirname "$0")
CGR_PROJECT=$BASEDIR/../../../

# Exit on error
set -ex

# Set proper version of npm
source ~/.nvm/nvm.sh
nvm install lts/erbium

# Update git
cd $CGR_PROJECT
git fetch

# Build front-end code
cd $CGR_PROJECT/georegistry-web/src/main/ng2
rm -rf node_modules
npm install
npm run build

# Build Java / webapp code
cd $CGR_PROJECT
mvn clean install -U
	
# Run Docker containers
sudo -E $CGR_PROJECT/src/dev/shell/postgres.sh
sudo -E $CGR_PROJECT/src/dev/shell/orientdb.sh

sleep 10

# Build database
cd $CGR_PROJECT/georegistry-server
mvn validate -P database -Ddb.clean=true -Ddb.rootPass=postgres -Ddb.rootUser=postgres -Ddb.rootDb=postgres -Ddb.patch=false

echo "Your environment is now built. If you would like to import test data you may run the 'CambodiaTestDataset' Java file."
