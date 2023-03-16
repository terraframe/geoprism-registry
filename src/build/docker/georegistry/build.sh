#!/bin/bash
#
# Arguments:
# $1 : Optional, if set to 'false' then we will not save the image to a file afterwords.
#

# Run this with sudo
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

set -e

# If tag is not set, then set it to 'latest' as a default value.
tag=${tag:-'latest'}

([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../georegistry-web/target/georegistry.war target/georegistry.war
cp -R ../../../../envcfg/prod target/appcfg

docker build -t terraframe/geoprism-registry:$tag .

if [ "${CGR_RELEASE_VERSION:-'latest'}" != "latest" ]; then
  docker tag terraframe/geoprism-registry:$tag terraframe/geoprism-registry:latest
fi

if [ "$1 != 'false'" ]; then
  docker save terraframe/geoprism-registry:$tag | gzip > target/georegistry.dimg.gz
fi
