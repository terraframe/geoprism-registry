#
# Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
#
# This file is part of Geoprism Registry(tm).
#
# Geoprism Registry(tm) is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Geoprism Registry(tm) is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
#

# This tells the build which version of npm to use:
. $NVM_DIR/nvm.sh && nvm install lts/erbium

export ANSIBLE_HOST_KEY_CHECKING=false

:
: ----------------------------------
:  Build and test
: ----------------------------------
:

cd $WORKSPACE/adapter/java

set +e
mvn clean install -B
aecode=$?
mkdir -p $TEST_OUTPUT/adapter/surefire-reports && cp $WORKSPACE/adapter/java/common/target/surefire-reports/* $TEST_OUTPUT/adapter/surefire-reports/ && chmod 777 -R $TEST_OUTPUT
set -e
[ "$aecode" != 0 ] && exit $aecode;

## Build angular source ##
npm version
cd $WORKSPACE/georegistry/georegistry-web/src/main/ng2
npm install
node -v && npm -v
node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile

## Run the tests ##
cd $WORKSPACE/georegistry
mvn install -B
cd $WORKSPACE/georegistry/georegistry-server
mvn install -B -P database -Ddb.clean=true -Ddatabase.port=5432 -Ddb.patch=false -Ddb.rootUser=postgres -Ddb.rootPass=postgres -Ddb.rootDb=postgres
cd $WORKSPACE/georegistry/georegistry-test
set +e
mvn test -Dappcfg=$WORKSPACE/georegistry/envcfg/dev -Ddatabase.port=5432 -Dproject.basedir=$WORKSPACE/georegistry
ecode=$?
mkdir -p $TEST_OUTPUT/georegistry-test/surefire-reports && cp $WORKSPACE/georegistry/georegistry-test/target/surefire-reports/* $TEST_OUTPUT/georegistry-test/surefire-reports/ && chmod 777 -R $TEST_OUTPUT
set -e
[ "$ecode" != 0 ] && exit $ecode;

## Deploy the test results to s3 ##
cd $WORKSPACE/georegistry/georegistry-site
sed -i -e 's#../../common-geo-registry-adapter#../../adapter#g' pom.xml
mvn surefire-report:report-only -Daggregate=true
mvn site -DgenerateReports=false
mvn -rf net.geoprism:georegistry-site site:deploy -DgenerateReports=false

exit 0;