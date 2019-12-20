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
package net.geoprism.registry.conversion;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.runwaysdk.business.Business;

import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.postgres.LeafServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.postgres.LeafGeoObjectQuery;
import net.geoprism.registry.service.RegistryIdService;

public class LeafGeoObjectStrategy extends LocalizedValueConverter implements ServerGeoObjectStrategyIF
{
  private ServerGeoObjectType type;

  public LeafGeoObjectStrategy(ServerGeoObjectType type)
  {
    super();

    this.type = type;
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.type;
  }

  @Override
  public LeafServerGeoObject constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(geoObject.getUid(), geoObject.getType());

      Business business = Business.get(runwayId);

      return new LeafServerGeoObject(type, business);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      Business business = new Business(type.definesType());

      return new LeafServerGeoObject(type, business);
    }
  }
  
  @Override
  public LeafServerGeoObject constructFromGeoObjectOverTime(GeoObjectOverTime goTime, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(goTime.getUid(), goTime.getType());

      Business business = Business.get(runwayId);

      return new LeafServerGeoObject(type, business);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(goTime.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(goTime.getUid());
        throw ex;
      }

      Business business = new Business(type.definesType());

      return new LeafServerGeoObject(type, business);
    }
  }

  @Override
  public LeafServerGeoObject constructFromDB(Object dbObject)
  {
    Business business = (Business) dbObject;

    return new LeafServerGeoObject(type, business);
  }

  @Override
  public ServerGeoObjectIF getGeoObjectByCode(String code)
  {
    Business business = LeafServerGeoObject.getByCode(type, code);

    if (business != null)
    {
      return new LeafServerGeoObject(type, business);
    }

    return null;
  }

  @Override
  public ServerGeoObjectIF newInstance()
  {
    Business business = new Business(type.definesType());

    return new LeafServerGeoObject(type, business);
  }

  @Override
  public ServerGeoObjectQuery createQuery(Date date)
  {
    return new LeafGeoObjectQuery(type);
  }

}
