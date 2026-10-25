# Spark version must match apache.spark in the root pom.xml (Spark Connect client)
ARG SPARK_VERSION=4.1.0
FROM apache/spark:${SPARK_VERSION}

ARG SEDONA_VERSION=1.9.1
ARG GEOTOOLS_WRAPPER_VERSION=1.9.1-33.5
ARG SPARK_COMPAT=4.1
ARG SCALA_VERSION=2.13

USER root

ADD --chmod=644 https://repo1.maven.org/maven2/org/apache/sedona/sedona-spark-shaded-${SPARK_COMPAT}_${SCALA_VERSION}/${SEDONA_VERSION}/sedona-spark-shaded-${SPARK_COMPAT}_${SCALA_VERSION}-${SEDONA_VERSION}.jar /opt/spark/jars/
ADD --chmod=644 https://repo1.maven.org/maven2/org/datasyslab/geotools-wrapper/${GEOTOOLS_WRAPPER_VERSION}/geotools-wrapper-${GEOTOOLS_WRAPPER_VERSION}.jar /opt/spark/jars/

RUN mkdir -p /opt/spark/conf /opt/spark/ivy && chmod 755 /opt/spark/conf && chown spark:spark /opt/spark/ivy
COPY --chmod=644 spark-defaults.conf /opt/spark/conf/spark-defaults.conf

USER spark
