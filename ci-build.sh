# Replace external ips with internal ips since jenkins runs inside our VPC
sed -i -e 's/georegistry.geoprism.net/172.31.30.53/g' geoprism-platform/ansible/inventory/georegistry/prod.ini
sed -i -e 's/staging-georegistry.geoprism.net/172.31.23.142/g' geoprism-platform/ansible/inventory/georegistry/staging.ini
sed -i -e "s/clean_db=true/clean_db=$clean_db/g" geoprism-platform/ansible/inventory/georegistry/$environment.ini
sed -i -e "s/clean_db=false/clean_db=$clean_db/g" geoprism-platform/ansible/inventory/georegistry/$environment.ini

source /home/ec2-user/ansible/hacking/env-setup

export M2_HOME=/usr/local/apache-maven
export M2=$M2_HOME/bin 
export PATH=$M2:$PATH
export ANSIBLE_HOST_KEY_CHECKING=false


:
: ----------------------------------
:  Build and test
: ----------------------------------
:
if [ "$build_artifact" == "true" ]; then
cd $WORKSPACE/adapter/java
mvn clean deploy -B

## Build angular source ##
cd $WORKSPACE/geoprism/geoprism-web/src/main/ng2
npm install
npm install typings
typings install lodash
npm run build
cd $WORKSPACE/georegistry/georegistry-web/src/main/ng2
npm install
npm install typings
typings install lodash
npm run build

if [ "$run_tests" == "true" ]; then
  ## Run the tests ##
  cd $WORKSPACE/georegistry
  mvn install -B
  cd $WORKSPACE/georegistry/georegistry-server
  mvn install -B -P patch -Droot.clean=true -Ddatabase.port=5432
  cd $WORKSPACE/georegistry/georegistry-test
  mvn test -Dappcfg=$WORKSPACE/georegistry/envcfg/dev -Ddatabase.port=5432

  ## Deploy the test results to s3 ##
  cd $WORKSPACE/georegistry/georegistry-site
  sed -i -e 's#../../common-geo-registry-adapter#../../adapter#g' pom.xml
  mvn surefire-report:report-only -Daggregate=true
  mvn site -DgenerateReports=false
  mvn -rf net.geoprism:georegistry-site site:deploy -DgenerateReports=false
fi

cd $WORKSPACE/georegistry
mvn clean deploy -B
fi


:
: ----------------------------------
:  Deploy to georegistry.geoprism.net
: ----------------------------------
:
if [ "$deploy" == "true" ]; then
cd $WORKSPACE/geoprism-platform/ansible

[ -e ./roles ] && unlink ./roles
ln -s $WORKSPACE/geoprism-cloud/ansible/roles ./roles
[ -e ./georegistry.yml ] && unlink ./georegistry.yml
ln -s $WORKSPACE/geoprism-cloud/ansible/georegistry.yml ./georegistry.yml

sudo chmod 400 ../permissions/geoprism-dev.pem
sudo chmod 400 ../permissions/geoprism-staging.pem
sudo chmod 400 ../permissions/georegistry-singapore.pem
pip install boto

ansible-playbook georegistry.yml -i inventory/georegistry/$environment.ini
fi