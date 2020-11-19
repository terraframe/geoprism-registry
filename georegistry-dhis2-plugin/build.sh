#!/bin/bash

set -e
set -x

[ -d target ] && rm -rf target
mkdir target

cp -r src/* target

cd target
zip -r cgr-dhis2-app.zip .
cd ..

[ -e ../../georegistry-server/src/main/resources/cgr-dhis2-app.zip ] && rm ../../georegistry-server/src/main/resources/cgr-dhis2-app.zip
cp target/cgr-dhis2-app.zip ../georegistry-server/src/main/resources/cgr-dhis2-app.zip
