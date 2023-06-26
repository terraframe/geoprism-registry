#!/bin/bash
#
#
#


# This script is designed to build and deploy the CGR to a local docker instance.
# Arg $1 can be "clean", at which point it rebuilds the database and runs maven with clean
# Requires environment variables: 
#   CGR - The path to your georegistry workspace. The root of your GPR project should be at $CGR/georegistry
#   MAVEN_HOME - Standard stuff. Google it

# Exit immediately if anything errors out
set -ex

# Run this with sudo
if [ "$EUID" -ne 0 ]
  then echo "Please run as root (with -E flag to include user variables)"
  exit
fi

export WORKSPACE=$CGR/georegistry

# Stop any existing database containers
docker stop cgr-postgres
docker stop cgr-orientdb

# Build the GPR
cd $WORKSPACE
if [ "$1" != 'clean' ]; then
  $MAVEN_HOME/bin/mvn install
else
  rm -rf /data
  $MAVEN_HOME/bin/mvn clean install
fi

# Build & run the docker image
cd $WORKSPACE/src/build/docker/georegistry
./build.sh false
docker compose up
