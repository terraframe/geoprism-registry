#!/bin/bash
# Run with elevated 'sudo' permissions as necessary

set -e

docker tag geoprism-registry:latest terraframe/geoprism-registry:latest

echo $DOCKER_HUB_TFBUILDER_PSW | docker login --username $DOCKER_HUB_TFBUILDER_USR --password-stdin terraframe/geoprism-registry

docker push terraframe/geoprism-registry:latest
