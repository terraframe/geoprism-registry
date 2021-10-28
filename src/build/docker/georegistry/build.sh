#!/bin/bash
# Run with elevated 'sudo' permissions as necessary

set -e

([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../georegistry-web/target/georegistry.war target/georegistry.war
cp -R ../../../../envcfg/prod target/appcfg

docker build -t terraframe/geoprism-registry:$CGR_RELEASE_VERSION .

if [ "$CGR_RELEASE_VERSION" != "latest" ]; then
  docker tag terraframe/geoprism-registry:$CGR_RELEASE_VERSION terraframe/geoprism-registry:latest
fi

docker save terraframe/geoprism-registry:$CGR_RELEASE_VERSION | gzip > target/georegistry.dimg.gz
