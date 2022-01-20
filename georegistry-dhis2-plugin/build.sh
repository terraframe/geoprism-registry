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


set -e
set -x

[ -d target ] && rm -rf target
mkdir target

cp -r src/* target

cd target
zip -r cgr-dhis2-app.zip .
cd ..

[ -e ../../georegistry-server/src/main/resources/cgr-dhis2-app.zip ] && rm ../../georegistry-server/src/main/resources/cgr-dhis2-app.zip
cp target/cgr-dhis2-app.zip ../georegistry-server/src/main/resources/cgr-dhis2-app.zip
