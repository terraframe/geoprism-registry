#!/bin/bash
# Run with elevated 'sudo' permissions as necessary

set -e

docker build -t georegistry .

docker tag georegistry:latest terraframe/geoprism-registry:latest

echo $DOCKER_HUB_TFBUILDER_PASSWORD | docker login --username $DOCKER_HUB_TFBUILDER_USERNAME --password-stdin terraframe/geoprism-registry

docker push terraframe/geoprism-registry:latest
