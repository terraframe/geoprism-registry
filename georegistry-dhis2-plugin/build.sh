#!/bin/bash

set -e
set -x

[ -d target ] && rm -rf target
mkdir target

cp -r src/* target

cd target
zip -r cgr-dhis2-app.zip .
cd ..
