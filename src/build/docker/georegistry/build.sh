#!/bin/bash
#
#
#

# Run with elevated 'sudo' permissions as necessary

set -e

# If tag is not set, then set it to 'latest' as a default value.
tag=${tag:-'latest'}

([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../georegistry-web/target/georegistry.war target/georegistry.war
cp -R ../../../../envcfg/prod target/appcfg

docker build -t terraframe/geoprism-registry:$tag .

if [ "$CGR_RELEASE_VERSION" != "latest" ]; then
  docker tag terraframe/geoprism-registry:$tag terraframe/geoprism-registry:latest
fi

docker save terraframe/geoprism-registry:$tag | gzip > target/georegistry.dimg.gz