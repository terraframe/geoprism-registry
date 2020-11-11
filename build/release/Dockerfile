FROM centos:8

RUN yum install -y unzip which zip python3 wget git

# Install SDKMan
RUN curl -s "https://get.sdkman.io" | bash

ENV SDKMAN_DIR=/root/.sdkman

RUN source "/root/.sdkman/bin/sdkman-init.sh" \
    && yes | sdk update \
    && yes | sdk install java 8.0.265-open \
    && yes | sdk install gradle 5.4.1 \
    && yes | sdk install maven

ENV JAVA_HOME="$SDKMAN_DIR/candidates/java/current"
ENV MAVEN_HOME="$SDKMAN_DIR/candidates/maven/current"
ENV GRADLE_HOME="$SDKMAN_DIR/candidates/gradle/current"
ENV PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$GRADLE_HOME/bin:$PATH"

# Install virtualenv and pipx
RUN python3 -m pip install --upgrade pip
RUN python3 -m pip install pipx
RUN python3 -m pipx ensurepath
RUN pipx install virtualenv

# Make sure our server time is always up to date (otherwise our nexus deploy gets wacky)
RUN yum install -y chrony
RUN chkconfig chronyd on

# Install Github CLI
RUN wget https://cli.github.com/packages/rpm/gh-cli.repo -O /etc/yum.repos.d/gh-cli.repo
RUN yum install -y gh

# Install git-lfs
RUN curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.rpm.sh | bash
RUN yum -y install git-lfs
RUN git lfs install

# Add nexus internal ip to /etc/hosts so we know how to reach it.
RUN sh -c "echo 'ec2-52-25-68-156.us-west-2.compute.amazonaws.com nexus.terraframe.com' >> /etc/hosts"

# Start up Postgres + Orient Docker containers
#RUN docker run --name postgres -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 mdillon/postgis:9.5
#RUN docker run -d --name orientdb -p 2424:2424 -p 2480:2480  -e ORIENTDB_ROOT_PASSWORD=root orientdb:3.0.25

RUN wget https://dl.google.com/android/repository/commandlinetools-linux-6858069_latest.zip -O /tmp/android-studio.zip \
    && mkdir -p /tmp/android-sdk/cmdline-tools \
    && unzip -C /tmp/android-studio.zip -d /tmp/android-sdk \
    && mkdir -p /opt/android-sdk/cmdline-tools/latest && mv /tmp/android-sdk/cmdline-tools/* /opt/android-sdk/cmdline-tools/latest

ENV ANDROID_HOME=/opt/android-sdk
RUN yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

ENTRYPOINT /bin/sh /workspace/build/release/ci-release-docker.sh
