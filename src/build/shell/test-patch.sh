#!/bin/bash
#
#
#


# Do not run this as sudo


# Careful with running this as it will delete all data at /data/postgres and /data/orientdb before running.

# What version are we patching from?
# TODO: Automatically run the last 3 major versions
export PATCH_FROM_VERSION=0.19

# Exit on errors
set -e

# Print the line as we execute it
set -x

# Shut down any lingering postgres or orientdb servers that may be currently running
sudo docker rm -f $(sudo docker ps -a -q --filter="name=postgres") || true
sudo docker rm -f $(sudo docker ps -a -q --filter="name=orientdb") || true
sudo docker rm -f $(sudo docker ps -a -q --filter="name=orientdb-initializer") || true

# Build the CGR
sudo docker run \
  --rm \
  --user 1000:1000 \
  -v ~/.m2:/var/maven/.m2 \
  -e MAVEN_CONFIG=/var/maven/.m2 \
  -v $WORKSPACE/georegistry:/workspace \
  -w /workspace \
  maven:3-openjdk-11-slim mvn clean install -B

# Build a Docker image
cd $WORKSPACE/georegistry/src/build/docker/georegistry
sudo ./build.sh

# Set up for testing
cd $WORKSPACE/georegistry/src/build/shell
[ -d target ] && cd target && sudo docker-compose down && cd .. && sudo rm -rf target
[ -d /data/postgres ] && sudo rm -rf /data/postgres
[ -d /data/orientdb ] && sudo rm -rf /data/orientdb
mkdir target

export AWS_ACCESS_KEY_ID=$TF_BUILDER_KEY
export AWS_SECRET_ACCESS_KEY=$TF_BUILDER_SECRET

# Install docker-compose if its not installed already
command -v docker-compose || (curl -SL https://github.com/docker/compose/releases/download/v2.7.0/docker-compose-linux-x86_64 -o target/docker-compose && sudo chmod +x target/docker-compose && sudo mv target/docker-compose /usr/bin)

# Download test database data
aws s3 cp s3://terraframe-builder/georegistry/test-patching-backups/$PATCH_FROM_VERSION/orientdb.backup.json.gz target/orientdb.backup.json.gz
gunzip target/orientdb.backup.json.gz
aws s3 cp s3://terraframe-builder/georegistry/test-patching-backups/$PATCH_FROM_VERSION/postgres.backup.gz target/postgres.backup.gz
gunzip target/postgres.backup.gz

wget https://raw.githubusercontent.com/terraframe/geoprism-registry/master/src/build/docker/georegistry/docker-compose.yml -O target/docker-compose.yml
wget https://github.com/mikefarah/yq/releases/download/v4.27.2/yq_linux_amd64.tar.gz -O - |  tar xz && mv yq_linux_amd64 target/yq

# Load testdb into postgres on container bootup
echo "psql -c \"CREATE USER georegistry WITH PASSWORD 'georegistry'\"" > target/postgres-init.sh
echo "pg_restore -U postgres -d georegistry < /tmp/data/postgres.backup" >> target/postgres-init.sh
target/yq -i ".services.postgres.volumes += [\"$(pwd)/target/postgres-init.sh:/docker-entrypoint-initdb.d/11_testdb.sh\"]" target/docker-compose.yml
target/yq -i ".services.postgres.volumes += [\"$(pwd)/target/postgres.backup:/tmp/data/postgres.backup\"]" target/docker-compose.yml

# Load testdb into orientdb
cd target && sudo docker-compose up -d orientdb && sleep 15 && cd ..
sudo docker run --rm -e ORIENTDB_ROOT_PASSWORD=root --network=host -v "$(pwd)/target/orientdb.backup.json:/tmp/data/orientdb.backup.json" orientdb:3.2 /orientdb/bin/console.sh CREATE DATABASE remote:localhost/georegistry root root plocal
sudo docker run --rm -e ORIENTDB_ROOT_PASSWORD=root --network=host -v "$(pwd)/target/orientdb.backup.json:/tmp/data/orientdb.backup.json" orientdb:3.2 /orientdb/bin/console.sh "connect remote:localhost/georegistry root root; import database /tmp/data/orientdb.backup.json; UPDATE OUser SET password = \"admin\" WHERE name = \"admin\""

# Boot the server
cd target && sudo docker-compose up -d && cd ..
sleep 180

# Grab the logs
set +x
REGISTRY_LOGS="$(sudo docker logs georegistry 2>&1)"
set -x

# Check for errors

set +x
set +e

# Must not contain any startup listeners which failed to start
echo "$REGISTRY_LOGS" | grep "One or more listeners failed to start." > /dev/null
[ $? -ne 1 ] && echo "Patch test failed on version $PATCH_FROM_VERSION due to server boot failure (listener failed to start)." && echo $REGISTRY_LOGS && exit 1

# Must contain cascading errors when it was trying to shutdown our app
echo "$REGISTRY_LOGS" | grep "but failed to remove it when the web application was stopped." > /dev/null
[ $? -ne 1 ] && echo "Patch test failed on version $PATCH_FROM_VERSION due to server boot failure (cascading app stop errors)." && echo $REGISTRY_LOGS && exit 1

# Must contain catalina saying that tomcat is running
echo "$REGISTRY_LOGS" | grep "org.apache.catalina.startup.Catalina.start Server startup in" > /dev/null
[ $? -ne 0 ] && echo "Patch test failed on version $PATCH_FROM_VERSION due to server boot failure (catalina server startup not found)." && echo $REGISTRY_LOGS && exit 1

# Must contain catalina saying that it has deployed the root application
echo "$REGISTRY_LOGS" | grep "org.apache.catalina.startup.HostConfig.deployDirectory Deploying web application directory \[/usr/local/tomcat/webapps/ROOT\]" > /dev/null
[ $? -ne 0 ] && echo "Patch test failed on version $PATCH_FROM_VERSION due to server boot failure (catalina root deployment not found)." && echo $REGISTRY_LOGS && exit 1

echo "Server successfully booted and patched from version $PATCH_FROM_VERSION. Test passed."

# Clean up
cd target && sudo docker-compose down && cd ..


# Export the patched database and upload to S3
#sudo docker run --rm -e ORIENTDB_ROOT_PASSWORD=root --network=host -v "$(pwd)/target/orientdb.backup.json:/tmp/data/orientdb.backup.json" orientdb:3.2 /orientdb/bin/console.sh "connect remote:localhost/georegistry root root; export database /tmp/data/orientdb.backup.json"
#sudo docker exec georegistry-postgres bash -c 'PGPASSWORD="root" /usr/bin/pg_dump --host localhost --port 5432 --username "postgres" --format custom --blobs --password --verbose --file "/tmp/data/postgres.backup" "georegistry"'

#export PATCH_TO_VERSION=0.19.7
#aws s3 cp target/orientdb.backup.json.gz s3://terraframe-builder/georegistry/test-patching-backups/$PATCH_TO_VERSION/orientdb.backup.json.gz
#aws s3 cp target/postgres.backup.gz s3://terraframe-builder/georegistry/test-patching-backups/$PATCH_TO_VERSION/postgres.backup.gz
