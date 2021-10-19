#!/bin/bash
# Run with elevated 'sudo' permissions as necessary

set -e

([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../georegistry-web/target/georegistry.war target/georegistry.war
cp -R ../../../../envcfg/prod target/appcfg

docker build -t geoprism-registry .
docker save geoprism-registry:latest | gzip > target/georegistry.dimg.gz
