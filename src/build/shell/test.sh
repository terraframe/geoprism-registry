#
#
#

# This tells the build which version of npm to use:
. $NVM_DIR/nvm.sh && nvm install lts/hydrogen

export ANSIBLE_HOST_KEY_CHECKING=false

:
: ----------------------------------
:  Build and test
: ----------------------------------
:

## Build angular source ##
npm version
cd $WORKSPACE/georegistry/georegistry-ui
npm install --force
node -v && npm -v
  #node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile
npm run build  

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
mkdir -p $TEST_OUTPUT/adapter/surefire-reports && cp $WORKSPACE/georegistry/georegistry-adapter/java/common/target/surefire-reports/* $TEST_OUTPUT/adapter/surefire-reports/ && chmod 777 -R $TEST_OUTPUT
mkdir -p $TEST_OUTPUT/dhis2adapter/surefire-reports && cp $WORKSPACE/georegistry/dhis2adapter/target/surefire-reports/* $TEST_OUTPUT/dhis2adapter/surefire-reports/ && chmod 777 -R $TEST_OUTPUT
set -e
[ "$ecode" != 0 ] && exit $ecode;

## Deploy the test results to s3 ##
cd $WORKSPACE/georegistry/georegistry-site
mvn surefire-report:report-only -Daggregate=true
mvn site -DgenerateReports=false
mvn -rf net.geoprism:georegistry-site site:deploy -DgenerateReports=false

exit 0;