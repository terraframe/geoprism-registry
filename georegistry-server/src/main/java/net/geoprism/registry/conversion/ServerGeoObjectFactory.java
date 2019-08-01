/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.conversion;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.business.Business;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerLeafGeoObject;
import net.geoprism.registry.model.ServerTreeGeoObject;
import net.geoprism.registry.service.RegistryIdService;

public class ServerGeoObjectFactory
{
  public static ServerGeoObjectIF getGeoObject(GeoObject go)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());

    if (type.isLeaf())
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(go.getUid(), go.getType());
      Business business = Business.get(runwayId);

      return new ServerLeafGeoObject(type, go, business);
    }
    else
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(go.getUid(), go.getType());
      GeoEntity geoEntity = GeoEntity.get(runwayId);
      Business business = ServerTreeGeoObject.getBusiness(geoEntity);

      return new ServerTreeGeoObject(type, go, geoEntity, business);
    }
  }
}
