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


# Before running this script, you must have set the following environment variables: #
# CGR_RELEASE_VERSION
# CGR_NEXT_VERSION
# NEXUS_ADMIN_USERNAME
# NEXUS_ADMIN_PASSWORD
# release_adapter, release_georegistry, tag_platform, tag_cloud, release_github

#if [ "$release_adapter" == "true" ]; then
  #if curl -f -s --head "https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/com/cgr/adapter/cgradapter-common/$CGR_RELEASE_VERSION/cgradapter-common-$CGR_RELEASE_VERSION.war" | head -n 1 | grep "HTTP/1.[01] [23].." > /dev/null; then
  #  echo "The release version $CGR_RELEASE_VERSION has already been deployed! Please ensure you are releasing the correct version."
  #  exit 1
  #fi
#fi

git config --global user.name "$GIT_TF_BUILDER_USERNAME"
git config --global user.email builder@terraframe.com

. $NVM_DIR/nvm.sh && nvm install lts/erbium

if [ "$release_adapter" == "true" ]; then
  #### CGR Adapter ####
  cd $WORKSPACE/adapter/java
  
  # CGR Adapter : License Headers
  git checkout master
  git pull
  git merge origin/dev
  mvn license:format -B
  git add -A
  git diff-index --quiet HEAD || git commit -m 'License headers'
  
  if [ "$dry_run" == "false" ]; then
    git push
  else
    git reset --hard
    git clean -fdx
  fi
  
  # CGR Adapter : Release
  mvn release:prepare -B -DdryRun=$dry_run -Dtag=$CGR_RELEASE_VERSION \
                   -DreleaseVersion=$CGR_RELEASE_VERSION \
                   -DdevelopmentVersion=$CGR_NEXT_VERSION
  mvn release:perform -B -DdryRun=$dry_run -Darguments="-Dmaven.javadoc.skip=true -Dmaven.site.skip=true"
  
  if [ "$dry_run" == "true" ]; then
    mvn versions:set -DnewVersion=$CGR_RELEASE_VERSION
    mvn clean install
  fi
  
  # CGR Adapter : Gradle Android Release
  sed -i -E "s/implementation 'com.cgr.adapter:cgradapter-common:.*'/implementation 'com.cgr.adapter:cgradapter-common:$CGR_RELEASE_VERSION'/g" $WORKSPACE/adapter/java/android/cgradapter_android/build.gradle
  sed -i -E "s/VERSION_NAME=.*/VERSION_NAME=$CGR_RELEASE_VERSION/g" $WORKSPACE/adapter/java/android/gradle.properties
  cd $WORKSPACE/adapter/java/android/cgradapter_android
  
  if [ "$dry_run" == "false" ]; then
    gradle -PNEXUS_USERNAME=$NEXUS_ADMIN_USERNAME -PNEXUS_PASSWORD=$NEXUS_ADMIN_PASSWORD clean build uploadArchives
  else
    gradle --dry-run -PNEXUS_USERNAME=$NEXUS_ADMIN_USERNAME -PNEXUS_PASSWORD=$NEXUS_ADMIN_PASSWORD clean build uploadArchives
  fi
  
  sed -i -E "s/implementation 'com.cgr.adapter:cgradapter-common:.*'/implementation 'com.cgr.adapter:cgradapter-common:$CGR_NEXT_VERSION'/g" $WORKSPACE/adapter/java/android/cgradapter_android/build.gradle
  sed -i -E "s/VERSION_NAME=.*/VERSION_NAME=$CGR_NEXT_VERSION/g" $WORKSPACE/adapter/java/android/gradle.properties
else
  mkdir -p $WORKSPACE/common-geo-registry-adapter/java/common/target && wget -nv -O $WORKSPACE/common-geo-registry-adapter/java/common/target/cgradapter-common-$CGR_RELEASE_VERSION.jar "https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/com/cgr/adapter/cgradapter-common/$CGR_RELEASE_VERSION/cgradapter-common-$CGR_RELEASE_VERSION.jar"
  mkdir -p $WORKSPACE/common-geo-registry-adapter/java/android/cgradapter_android/target && wget -nv -O $WORKSPACE/common-geo-registry-adapter/java/android/cgradapter_android/target/cgradapter-android-$CGR_RELEASE_VERSION.aar "https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/com/cgr/adapter/cgradapter-android/$CGR_RELEASE_VERSION/cgradapter-android-$CGR_RELEASE_VERSION.aar"
fi

if [ "$release_georegistry" == "true" ]; then
  #### Georegistry ####
  cd $WORKSPACE/georegistry
  
  # Georegistry : Hardcode dependent library versions (adapter)
  git checkout master
  git pull
  git merge origin/dev
  sed -i -E "s_<cgr.adapter.version>.*</cgr.adapter.version>_<cgr.adapter.version>$CGR_RELEASE_VERSION</cgr.adapter.version>_g" georegistry-server/pom.xml
  cd georegistry-web/src/main/ng2
  npm install
  node -v && npm -v
  node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile
  cd $WORKSPACE/georegistry
  git add -A
  git diff-index --quiet HEAD || git commit -m 'Preparing project for release.'
  if [ "$dry_run" == "false" ]; then
    git push
  else
    git reset --hard
    git clean -fdx
  fi
  
  # Georegistry : License Headers
  cd $WORKSPACE/georegistry
  git checkout master
  mvn license:format -B
  git add -A
  git diff-index --quiet HEAD || git commit -m 'License headers'
  if [ "$dry_run" == "false" ]; then
    git push
  else
    git reset --hard
    git clean -fdx
  fi
  
  # Georegistry : Release
  cd $WORKSPACE/georegistry
  git checkout master
  mvn release:prepare -B -DdryRun=$dry_run -Dtag=$CGR_RELEASE_VERSION \
                   -DreleaseVersion=$CGR_RELEASE_VERSION \
                   -DdevelopmentVersion=$CGR_NEXT_VERSION
  mvn release:perform -B -DdryRun=$dry_run -Darguments="-Dmaven.javadoc.skip=true -Dmaven.site.skip=true"
  
  # Georegistry : Update dependent library versions for development
  cd $WORKSPACE
  rm -rf builderdev
  mkdir builderdev
  cd builderdev
  git clone -b master git@github.com:terraframe/geoprism-registry.git
  cd geoprism-registry
  git checkout master
  sed -i -E "s_<cgr.adapter.version>.*</cgr.adapter.version>_<cgr.adapter.version>$CGR_NEXT_VERSION</cgr.adapter.version>_g" georegistry-server/pom.xml
  sed -i -E "0,/<version>.*<\/version>/s/<version>.*<\/version>/<version>$CGR_NEXT_VERSION<\/version>/" pom.xml
  sed -i -E "0,/<version>.*<\/version>/s/<version>.*<\/version>/<version>$CGR_NEXT_VERSION<\/version>/" georegistry-server/pom.xml
  sed -i -E "0,/<version>.*<\/version>/s/<version>.*<\/version>/<version>$CGR_NEXT_VERSION<\/version>/" georegistry-web/pom.xml
  sed -i -E "0,/<version>.*<\/version>/s/<version>.*<\/version>/<version>$CGR_NEXT_VERSION<\/version>/" georegistry-test/pom.xml
  git add -A
  git diff-index --quiet HEAD || git commit -m 'Preparing dependencies for next development iteration.'
  if [ "$dry_run" == "false" ]; then
    git push
  else
    git reset --hard
    git clean -fdx
  fi
else
  mkdir -p $WORKSPACE/geoprism-registry/georegistry-web/target && wget -nv -O $WORKSPACE/geoprism-registry/georegistry-web/target/georegistry.war "https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-web/$CGR_RELEASE_VERSION/georegistry-web-$CGR_RELEASE_VERSION.war"
fi

if [ "$release_docker" == "true" ]; then
  cd $WORKSPACE/geoprism-registry/src/build/docker/georegistry
  ./build.sh
  
  if [ "$dry_run" == "false" ]; then
    ./release.sh
  fi
fi

if [ "$tag_platform" == "true" ]; then
  cd $WORKSPACE
  git clone -b master git@github.com:terraframe/geoprism-platform.git
  cd geoprism-platform
  git merge origin/dev
  
  if [ "$dry_run" == "false" ]; then
    git push
    git tag -a cgr-$CGR_RELEASE_VERSION -m "Deployment scripts for Common GeoRegistry version $CGR_RELEASE_VERSION"
    git push origin cgr-$CGR_RELEASE_VERSION
  else
    git tag -a cgr-$CGR_RELEASE_VERSION -m "Deployment scripts for Common GeoRegistry version $CGR_RELEASE_VERSION"
  fi
fi

if [ "$tag_cloud" == "true" ]; then
  cd $WORKSPACE
  #git clone -b master git@github.com:terraframe/geoprism-cloud.git
  cd geoprism-cloud
  git checkout master
  git merge origin/dev
  
  if [ "$dry_run" == "false" ]; then
    git push
    git tag -a cgr-$CGR_RELEASE_VERSION -m "Deployment scripts for Common GeoRegistry version $CGR_RELEASE_VERSION"
    git push origin cgr-$CGR_RELEASE_VERSION
  else
    git tag -a cgr-$CGR_RELEASE_VERSION -m "Deployment scripts for Common GeoRegistry version $CGR_RELEASE_VERSION"
  fi
fi

if [ "$release_github" == "true" ]; then
  cd $WORKSPACE/georegistry
  
  gh config set prompt disabled
  
  # TODO : We really should be using the artifacts we compiled earlier.
  sleep 180 # Cloudsmith takes a little bit to process the artifact before its downloadable.
  wget https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-web/$CGR_RELEASE_VERSION/georegistry-web-$CGR_RELEASE_VERSION.war -O georegistry-web-$CGR_RELEASE_VERSION.war
  wget https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/com/cgr/adapter/cgradapter-common/$CGR_RELEASE_VERSION/cgradapter-common-$CGR_RELEASE_VERSION.jar -O cgradapter-common-$CGR_RELEASE_VERSION.jar
  wget https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/com/cgr/adapter/cgradapter-android/$CGR_RELEASE_VERSION/cgradapter-android-$CGR_RELEASE_VERSION.aar -O cgradapter-android-$CGR_RELEASE_VERSION.aar
  
  if [ "$dry_run" == "false" ]; then
    gh release create $CGR_RELEASE_VERSION "georegistry-web-$CGR_RELEASE_VERSION.war#CGR Webapp" "cgradapter-common-$CGR_RELEASE_VERSION.jar#CGR Java Adapter" "cgradapter-android-$CGR_RELEASE_VERSION.aar#CGR Android Adapter"
  fi
  
  
  # Use the artifacts we compiled earlier
  # TODO : I cant figure out where gradle stores its aar after build
  # gh release create $CGR_RELEASE_VERSION "georegistry/georegistry-web-$CGR_RELEASE_VERSION.war#CGR Webapp" "common-geo-registry-adapter/java/common/target/cgradapter-common-$CGR_RELEASE_VERSION.jar#CGR Java Adapter" "common-geo-registry-adapter/java/android/cgradapter_android/target/cgradapter-android-$CGR_RELEASE_VERSION.aar#CGR Android Adapter"
fi
