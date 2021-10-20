#!/bin/bash
# Run with elevated 'sudo' permissions as necessary

set -e

docker tag terraframe/geoprism-registry:latest terraframe/geoprism-registry:$CGR_RELEASE_VERSION

echo $DOCKER_HUB_TFBUILDER_PSW | docker login --username $DOCKER_HUB_TFBUILDER_USR --password-stdin

docker push terraframe/geoprism-registry --all-tags
