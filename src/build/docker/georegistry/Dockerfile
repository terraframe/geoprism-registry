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

FROM tomcat:8.5-jdk8-openjdk

ENV CGR_VERSION=LATEST
ENV GEOSERVER_VERSION=2.20.2-terraframe
ENV LOG_LEVEL=error
ENV POSTGRES_PORT=5432

RUN mkdir -p $CATALINA_HOME/wars

# Embedded Geoserver
RUN wget -nv -O $CATALINA_HOME/wars/geoserver.war "https://dl.cloudsmith.io/public/terraframe/public/maven/org/geoserver/geoserver/$GEOSERVER_VERSION/geoserver-$GEOSERVER_VERSION.war"
RUN mkdir -p $CATALINA_HOME/webapps/geoserver && cd $CATALINA_HOME/webapps/geoserver && $JAVA_HOME/bin/jar xf $CATALINA_HOME/wars/geoserver.war

# Copy CGR WAR
COPY target/georegistry.war $CATALINA_HOME/wars/geoprism.war

# Deploy CGR War
RUN mkdir -p $CATALINA_HOME/wars/geoprism && cd $CATALINA_HOME/wars/geoprism && $JAVA_HOME/bin/jar xf $CATALINA_HOME/wars/geoprism.war
RUN mv $CATALINA_HOME/wars/geoprism $CATALINA_HOME/webapps/ROOT

# Log4j properties
RUN wget -nv -O $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2.xml https://raw.githubusercontent.com/terraframe/geoprism-cloud/dev/ansible/roles/webserver/files/log4j2.xml
RUN sed -i -e 's/<Root level="error">/<Root level="$LOG_LEVEL">/g' $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2.xml

# Pre-load with a default appcfg
COPY target/appcfg $CATALINA_HOME/appcfg

RUN wget -nv -O $CATALINA_HOME/bin/wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
RUN chmod +x $CATALINA_HOME/bin/wait-for-it.sh

#CMD ["$CATALINA_HOME/bin/wait-for-it.sh", "-t", 0, "database:$POSTGRES_PORT", "--"]
