/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package com.runwaysdk.build.domain;

import java.util.List;

import com.runwaysdk.build.DatabaseBuilder;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.session.Request;

public class ExistsAndInvalidInstanceDataPatchChecker
{
  public static void main(String[] args)
  {
    patchInstanceData();
  }
  
  @Request
  public static void patchInstanceData()
  {
    final String patchExistsAndInvalid = "0001625168828072";
    final String patchExistsAndInvalidInstanceData = "0001639003382316";
    
    List<String> values = com.runwaysdk.dataaccess.database.Database.getPropertyValue(DatabaseBuilder.RUNWAY_METADATA_VERSION_TIMESTAMP_PROPERTY);

    if (values.contains(patchExistsAndInvalid))
    {
      com.runwaysdk.dataaccess.database.Database.addPropertyValue(com.runwaysdk.dataaccess.database.Database.VERSION_NUMBER, MdAttributeCharacterInfo.CLASS, patchExistsAndInvalidInstanceData, DatabaseBuilder.RUNWAY_METADATA_VERSION_TIMESTAMP_PROPERTY);
    }
  }
}
