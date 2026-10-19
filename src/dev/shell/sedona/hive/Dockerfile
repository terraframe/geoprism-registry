# 4.0.1 is the newest metastore version supported by Spark 4.x
ARG HIVE_VERSION=4.0.1
FROM apache/hive:${HIVE_VERSION}

ARG POSTGRES_JDBC_VERSION=42.7.4

ADD --chmod=644 https://repo1.maven.org/maven2/org/postgresql/postgresql/${POSTGRES_JDBC_VERSION}/postgresql-${POSTGRES_JDBC_VERSION}.jar /opt/hive/lib/postgresql.jar
