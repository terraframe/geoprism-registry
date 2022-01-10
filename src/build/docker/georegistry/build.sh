#!/bin/bash
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

# Run with elevated 'sudo' permissions as necessary

set -e

([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../georegistry-web/target/georegistry.war target/georegistry.war
cp -R ../../../../envcfg/prod target/appcfg

docker build -t terraframe/geoprism-registry:$tag .

if [ "$CGR_RELEASE_VERSION" != "latest" ]; then
  docker tag terraframe/geoprism-registry:$tag terraframe/geoprism-registry:latest
fi

docker save terraframe/geoprism-registry:$tag | gzip > target/georegistry.dimg.gz