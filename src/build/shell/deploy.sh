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
if [ "$build_artifact" == "true" ]; then
  ## Build angular source ##
  npm version
  cd $WORKSPACE/georegistry/georegistry-ui
  npm install --force
  node -v && npm -v
  #node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile
  npm run build  
  
  cd $WORKSPACE/georegistry
  mvn clean install -B -Djavax.net.ssl.trustStore=$WORKSPACE/georegistry/georegistry-web/src/test/resources/tomcat.truststore -Djavax.net.ssl.trustStorePassword=2v8hVW2rPFncN6m -Djavax.net.ssl.keyStore=$WORKSPACE/georegistry/georegistry-web/src/test/resources/keystore.ks -Djavax.net.ssl.keyStorePassword=2v8hVW2rPFncN6m
else
  if [ "$tag" == "latest" ]; then
    # As far as I can tell Cloudsmith doesn't support fetching the latest version of an artifact from their REST API. So we're using Maven dependency:copy plugin.
    mkdir -p $WORKSPACE/georegistry/georegistry-web/target/artifact-download
    cp $WORKSPACE/georegistry/src/build/shell/artifact-download.pom.xml $WORKSPACE/georegistry/georegistry-web/target/artifact-download/pom.xml
    cd $WORKSPACE/georegistry/georegistry-web/target/artifact-download
    
    mvn dependency:copy -Dartifact=net.geoprism:georegistry-web:LATEST:war -DoutputDirectory=../ -Dmdep.stripVersion=true
    mv ../georegistry-web.war ../georegistry.war
  else
    mkdir -p $WORKSPACE/georegistry/georegistry-web/target && wget -nv -O $WORKSPACE/georegistry/georegistry-web/target/georegistry.war "https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-web/$tag/georegistry-web-$tag.war"
  fi
fi

#if [ "$tag" == "latest" ]; then
  # Build a Docker image
  cd $WORKSPACE/georegistry/src/build/docker/georegistry
  ./build.sh
#fi

:
: ----------------------------------
:  Deploy to georegistry.geoprism.net
: ----------------------------------
:
cd $WORKSPACE/geoprism-cloud/ansible

[ -h ./inventory ] && unlink ./inventory
[ -d ./inventory ] && rm -r ./inventory
ln -s $WORKSPACE/geoprism-platform/ansible/inventory ./inventory

[ -h ../permissions ] && unlink ../permissions
ln -s $WORKSPACE/geoprism-platform/permissions ../permissions

if [ "$environment" != "idm-alpha" ]; then
	if [ ! -z "$geoprism_lib_extension_artifact_name" ]; then
	  # As far as I can tell Cloudsmith doesn't support fetching the latest version of an artifact from their REST API. So we're using Maven dependency:copy plugin.
	  mkdir -p $WORKSPACE/georegistry/georegistry-web/target/artifact-download
	  cp $WORKSPACE/georegistry/src/build/shell/artifact-download.pom.xml $WORKSPACE/georegistry/georegistry-web/target/artifact-download/pom.xml
	  cd $WORKSPACE/georegistry/georegistry-web/target/artifact-download
	  mvn dependency:copy -Dartifact=$geoprism_lib_extension_artifact_package:$geoprism_lib_extension_artifact_name:$geoprism_lib_extension_artifact_version:jar -DoutputDirectory=../ -Dmdep.stripVersion=true
	  cd $WORKSPACE/geoprism-cloud/ansible
	  
	  ansible-playbook georegistry.yml -vv -i inventory/georegistry/$environment.ini --extra-vars "geoprism_lib_extension=$WORKSPACE/georegistry/georegistry-web/target/$geoprism_lib_extension_artifact_name.jar clean_db=$clean_db clean_orientdb=$clean_db webserver_docker_image_tag=$tag docker_image_path=../../georegistry/src/build/docker/georegistry/target/georegistry.dimg.gz"
	else
	  ansible-playbook georegistry.yml -vv -i inventory/georegistry/$environment.ini --extra-vars "clean_db=$clean_db clean_orientdb=$clean_db webserver_docker_image_tag=$tag docker_image_path=../../georegistry/src/build/docker/georegistry/target/georegistry.dimg.gz"
	fi
else
	ansible-playbook georegistry-ol7.yml -vv -i inventory/georegistry/$environment.ini --extra-vars "clean_db=$clean_db clean_orientdb=$clean_db webserver_docker_image_tag=$tag docker_image_path=../../georegistry/src/build/docker/georegistry/target/georegistry.dimg.gz"
fi

if [ "$environment" == "demo" ]; then
  ansible-playbook $WORKSPACE/geoprism-platform/ansible/aws/snapshot.yml -i inventory/georegistry/aws-$environment.ini
fi

exit 0;
