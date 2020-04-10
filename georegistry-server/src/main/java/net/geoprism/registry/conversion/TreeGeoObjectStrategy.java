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
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.postgres.TreeServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.postgres.TreeGeoObjectQuery;
import net.geoprism.registry.service.RegistryIdService;

public class TreeGeoObjectStrategy extends LocalizedValueConverter implements ServerGeoObjectStrategyIF
{
  private ServerGeoObjectType type;

  public TreeGeoObjectStrategy(ServerGeoObjectType type)
  {
    super();

    this.type = type;
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.type;
  }

  public TreeServerGeoObject constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(geoObject.getUid(), type.getType());

      GeoEntity entity = GeoEntity.get(runwayId);
      Business business = TreeServerGeoObject.getBusiness(entity);

      return new TreeServerGeoObject(type, entity, business);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      GeoEntity entity = new GeoEntity();
      entity.setUniversal(type.getUniversal());

      Business business = new Business(type.definesType());

      return new TreeServerGeoObject(type, entity, business);
    }
  }
  
  public TreeServerGeoObject constructFromGeoObjectOverTime(GeoObjectOverTime goTime, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(goTime.getUid(), type.getType());

      GeoEntity entity = GeoEntity.get(runwayId);
      Business business = TreeServerGeoObject.getBusiness(entity);

      return new TreeServerGeoObject(type, entity, business);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(goTime.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(goTime.getUid());
        throw ex;
      }

      GeoEntity entity = new GeoEntity();
      entity.setUniversal(type.getUniversal());

      Business business = new Business(type.definesType());

      return new TreeServerGeoObject(type, entity, business);
    }
  }

  @Override
  public TreeServerGeoObject constructFromDB(Object dbObject)
  {
    GeoEntity geoEntity = (GeoEntity) dbObject;
    Business business = TreeServerGeoObject.getBusiness(geoEntity);

    return new TreeServerGeoObject(type, geoEntity, business);
  }

  @Override
  public TreeServerGeoObject getGeoObjectByCode(String code)
  {
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      this.getType().enforceActorHasPermission(Session.getCurrentSession().getUser(), Operation.READ, true);
    }
    
    Business business = TreeServerGeoObject.getByCode(type, code);

    if (business != null)
    {
      String entityId = business.getValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
      GeoEntity entity = GeoEntity.get(entityId);

      return new TreeServerGeoObject(type, entity, business);
    }

    return null;
  }

  @Override
  public TreeServerGeoObject newInstance()
  {
    GeoEntity entity = new GeoEntity();
    entity.setUniversal(type.getUniversal());

    Business business = new Business(type.definesType());

    return new TreeServerGeoObject(type, entity, business);
  }

  @Override
  public ServerGeoObjectQuery createQuery(Date date)
  {
    return new TreeGeoObjectQuery(type);
  }
}
