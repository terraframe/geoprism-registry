#
#
#


# Before running this script, you must have set the following environment variables: #
# CGR_RELEASE_VERSION
# CGR_NEXT_VERSION
# NEXUS_ADMIN_USERNAME
# NEXUS_ADMIN_PASSWORD
# release_georegistry, tag_platform, tag_cloud, release_github

#if [ "$release_adapter" == "true" ]; then
  #if curl -f -s --head "https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-adapter/$CGR_RELEASE_VERSION/georegistry-adapter-$CGR_RELEASE_VERSION.war" | head -n 1 | grep "HTTP/1.[01] [23].." > /dev/null; then
  #  echo "The release version $CGR_RELEASE_VERSION has already been deployed! Please ensure you are releasing the correct version."
  #  exit 1
  #fi
#fi

export "MAVEN_OPTS=$MAVEN_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=warning"

set +x # Don't print every command before we run it
git config --global user.name "$GIT_TF_BUILDER_USERNAME"
git config --global user.email builder@terraframe.com

. $NVM_DIR/nvm.sh && nvm install lts/hydrogen
set -x # Go back to printing each command

if [ "$release_georegistry" == "true" ]; then
  #### Georegistry ####
  cd $WORKSPACE/georegistry
  
  # Georegistry : Hardcode dependent library versions (adapter)
  git checkout $release_branch
  git pull
  sed -i -E "s_<cgr.adapter.version>.*</cgr.adapter.version>_<cgr.adapter.version>$CGR_RELEASE_VERSION</cgr.adapter.version>_g" georegistry-server/pom.xml
  
  cd $WORKSPACE/georegistry
  git add -A
  git diff-index --quiet HEAD || git commit --no-verify -m "chore(release): preparing project for release $CGR_RELEASE_VERSION"
  if [ "$dry_run" == "false" ]; then
    git push
  else
    git reset --hard
    git clean -fdx
  fi
  
  # Georegistry : License Headers
  cd $WORKSPACE/georegistry
  git checkout $release_branch
  mvn license:format -B -q
  git add -A
  git diff-index --quiet HEAD || git commit --no-verify -m "chore(release): license headers for $CGR_RELEASE_VERSION"
  if [ "$dry_run" == "false" ]; then
    git push
  else
    git reset --hard
    git clean -fdx
  fi

  # Georegistry : Build front-end  
  cd $WORKSPACE/georegistry
  # git checkout $release_branch
  cd georegistry-ui
  npm install --force
  node -v && npm -v
  #node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile
  npm run build  
  
  
  # Georegistry : Release
  cd $WORKSPACE/georegistry
  git checkout $release_branch
  mvn release:prepare -B -e -X -q -DdryRun=$dry_run -Dtag=$CGR_RELEASE_VERSION \
                   -DreleaseVersion=$CGR_RELEASE_VERSION \
                   -DdevelopmentVersion=$CGR_NEXT_VERSION
  mvn release:perform -B -q -DdryRun=$dry_run -Darguments="-Dmaven.javadoc.skip=true -Dmaven.site.skip=true"
  
  # Generate Changelog
  cd $WORKSPACE/georegistry
  if [ "$dry_run" == "true" ]; then
    git tag $CGR_RELEASE_VERSION
  fi
  sed -i "0,/<version>.*<\/version>/s//<version>$CGR_RELEASE_VERSION<\/version>/" pom.xml
  mvn git-changelog-maven-plugin:git-changelog --non-recursive
  mv CHANGELOG.md CHANGELOG-old.md
  head -n 3 CHANGELOG-old.md > CHANGELOG.md
  echo "" >> CHANGELOG.md
  cat CHANGELOG2.md >> CHANGELOG.md
  tail -n +3 CHANGELOG-old.md >> CHANGELOG.md
  git add CHANGELOG.md
  git commit --no-verify -m "chore(release): update changelog for $CGR_RELEASE_VERSION"
  if [ "$dry_run" == "false" ]; then
    git push
  else
    echo "Generated changelog as:"
    cat CHANGELOG.md
    git reset --hard
  fi
else
  mkdir -p $WORKSPACE/georegistry/georegistry-web/target && wget -nv -O $WORKSPACE/georegistry/georegistry-web/target/georegistry.war "https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-web/$CGR_RELEASE_VERSION/georegistry-web-$CGR_RELEASE_VERSION.war"
fi

# Gradle release must happen after CGR release since it depends upon it
#if [ "$release_android" == "true" ]; then
#  cd $WORKSPACE/georegistry/georegistry-adapter/java
#  
#  # CGR Adapter : Gradle Android Release
#  sed -i -E "s/implementation 'net.geoprism:georegistry-adapter:.*'/implementation 'net.geoprism:georegistry-adapter:$CGR_RELEASE_VERSION'/g" $WORKSPACE/georegistry/georegistry-adapter/java/android/cgradapter_android/build.gradle
#  sed -i -E "s/VERSION_NAME=.*/VERSION_NAME=$CGR_RELEASE_VERSION/g" $WORKSPACE/georegistry/georegistry-adapter/java/android/gradle.properties
#  cd $WORKSPACE/georegistry/georegistry-adapter/java/android/cgradapter_android
#  
#  if [ "$dry_run" == "false" ]; then
#    gradle -PNEXUS_USERNAME=$NEXUS_ADMIN_USERNAME -PNEXUS_PASSWORD=$NEXUS_ADMIN_PASSWORD clean build uploadArchives
#  else
#    gradle --dry-run -PNEXUS_USERNAME=$NEXUS_ADMIN_USERNAME -PNEXUS_PASSWORD=$NEXUS_ADMIN_PASSWORD clean build uploadArchives
#  fi
#  
#  sed -i -E "s/implementation 'net.geoprism:georegistry-adapter:.*'/implementation 'net.geoprism:georegistry-adapter:$CGR_NEXT_VERSION'/g" $WORKSPACE/georegistry/georegistry-adapter/java/android/cgradapter_android/build.gradle
#  sed -i -E "s/VERSION_NAME=.*/VERSION_NAME=$CGR_NEXT_VERSION/g" $WORKSPACE/georegistry/georegistry-adapter/java/android/gradle.properties
#else
#  mkdir -p $WORKSPACE/georegistry/georegistry-adapter/java/android/georegistry_adapter_android/target && wget -nv -O $WORKSPACE/georegistry/georegistry-adapter/java/android/georegistry_adapter_android/target/georegistry-adapter-android-$CGR_RELEASE_VERSION.aar "https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-adapter-android/$CGR_RELEASE_VERSION/georegistry-adapter-android-$CGR_RELEASE_VERSION.aar"
#fi

if [ "$release_docker" == "true" ]; then
  cd $WORKSPACE/georegistry/src/build/docker/georegistry
  export tag=$CGR_RELEASE_VERSION
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
  
  if [ "$dry_run" == "false" ]; then
    # TODO : Use the artifacts we compiled earlier?
    sleep 180 # Cloudsmith takes a little bit to process the artifact before its downloadable.
    wget https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-web/$CGR_RELEASE_VERSION/georegistry-web-$CGR_RELEASE_VERSION.war -O georegistry-web-$CGR_RELEASE_VERSION.war
    #wget https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-adapter/$CGR_RELEASE_VERSION/georegistry-adapter-$CGR_RELEASE_VERSION.jar -O georegistry-adapter-$CGR_RELEASE_VERSION.jar
    #wget https://dl.cloudsmith.io/public/terraframe/geoprism-registry/maven/net/geoprism/georegistry-adapter-android/$CGR_RELEASE_VERSION/georegistry-adapter-android-$CGR_RELEASE_VERSION.aar -O georegistry-adapter-android-$CGR_RELEASE_VERSION.aar
  
    #gh release create $CGR_RELEASE_VERSION "georegistry-web-$CGR_RELEASE_VERSION.war#CGR Webapp" "georegistry-adapter-$CGR_RELEASE_VERSION.jar#CGR Java Adapter" "georegistry-adapter-android-$CGR_RELEASE_VERSION.aar#CGR Android Adapter"
    #gh release create $CGR_RELEASE_VERSION --notes-file CHANGELOG2.md "georegistry-web-$CGR_RELEASE_VERSION.war#CGR Webapp" "georegistry-adapter-$CGR_RELEASE_VERSION.jar#CGR Java Adapter"
    gh release create $CGR_RELEASE_VERSION --notes-file CHANGELOG2.md "georegistry-web-$CGR_RELEASE_VERSION.war#CGR Webapp"
  fi
  
  
  # Use the artifacts we compiled earlier
  # TODO : I cant figure out where gradle stores its aar after build
  # gh release create $CGR_RELEASE_VERSION "georegistry/georegistry-web-$CGR_RELEASE_VERSION.war#CGR Webapp" "common-geo-registry-adapter/java/common/target/cgradapter-common-$CGR_RELEASE_VERSION.jar#CGR Java Adapter" "common-geo-registry-adapter/java/android/cgradapter_android/target/cgradapter-android-$CGR_RELEASE_VERSION.aar#CGR Android Adapter"
fi
