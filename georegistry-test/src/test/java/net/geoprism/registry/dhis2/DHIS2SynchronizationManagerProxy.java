/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.dhis2;

import com.google.gson.JsonArray;

import net.geoprism.dhis2.dhis2adapter.response.ImportReportResponse;
import net.geoprism.registry.etl.DHIS2SyncLevel;

public class DHIS2SynchronizationManagerProxy
{
  public static void processMetadataImportResponse(DHIS2SynchronizationManager manager, DHIS2SyncLevel level, String submittedJson, ImportReportResponse resp, final JsonArray orgUnitsPayload, final JsonArray orgUnitGroupsPayload)
  {
    manager.processMetadataImportResponse(level, submittedJson, resp, orgUnitsPayload, orgUnitGroupsPayload);
  }
}
