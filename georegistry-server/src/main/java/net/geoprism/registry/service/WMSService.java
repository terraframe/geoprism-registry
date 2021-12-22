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
package net.geoprism.registry.service;

import java.util.List;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverService;
import net.geoprism.registry.MasterListVersion;

public class WMSService
{

  private static final String PREFIX  = "gs";

  GeoserverService            service = GeoserverFacade.getService();

  @Request
  public void createAllWMSLayers(boolean forceGeneration)
  {
    List<? extends MasterListVersion> versions = MasterListVersion.getAll(MasterListVersion.PUBLISHED);

    for (MasterListVersion version : versions)
    {
      this.createGeoServerLayer(version, forceGeneration);
    }
  }

  @Request
  public void deleteAllWMSLayers()
  {
    List<? extends MasterListVersion> versions = MasterListVersion.getAll(MasterListVersion.PUBLISHED);

    for (MasterListVersion version : versions)
    {
      this.deleteWMSLayer(version);
    }
  }

  @Request(RequestType.SESSION)
  public void createWMSLayer(String sessionId, MasterListVersion version, boolean forceGeneration)
  {
    this.createGeoServerLayer(version, forceGeneration);
  }

  public void createGeoServerLayer(MasterListVersion version, boolean forceGeneration)
  {
    String tableName = version.getMdBusiness().getTableName();

    if (forceGeneration)
    {
      service.forceRemoveLayer(tableName);
    }

    // Now that the database transaction is complete we can create the geoserver
    // layer
    if (!service.layerExists(tableName))
    {
      service.publishLayer(tableName, null);
    }
  }

  public void deleteWMSLayer(MasterListVersion version)
  {
    String tableName = version.getMdBusiness().getTableName();

    service.forceRemoveLayer(tableName);
  }
}
