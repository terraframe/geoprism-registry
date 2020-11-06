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

import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.InvalidRegistryIdException;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.RegistryIdService;

public class VertexGeoObjectStrategy extends LocalizedValueConverter implements ServerGeoObjectStrategyIF
{
  private ServerGeoObjectType type;

  public VertexGeoObjectStrategy(ServerGeoObjectType type)
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
  public VertexServerGeoObject constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      VertexObject vertex = VertexServerGeoObject.getVertex(type, geoObject.getUid());

      if (vertex == null)
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      return new VertexServerGeoObject(type, vertex);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      VertexObject vertex = VertexServerGeoObject.newInstance(type);

      return new VertexServerGeoObject(type, vertex);
    }
  }

  @Override
  public VertexServerGeoObject constructFromGeoObjectOverTime(GeoObjectOverTime goTime, boolean isNew)
  {
    if (!isNew)
    {
      VertexObject vertex = VertexServerGeoObject.getVertex(type, goTime.getUid());

      if (vertex == null)
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(goTime.getUid());
        throw ex;
      }

      return new VertexServerGeoObject(type, vertex);
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(goTime.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(goTime.getUid());
        throw ex;
      }

      VertexObject vertex = VertexServerGeoObject.newInstance(type);

      return new VertexServerGeoObject(type, vertex);
    }
  }

  @Override
  public VertexServerGeoObject constructFromDB(Object dbObject)
  {
    VertexObject vertex = (VertexObject) dbObject;

    return new VertexServerGeoObject(type, vertex);
  }

  @Override
  public VertexServerGeoObject getGeoObjectByCode(String code)
  {
    VertexObject vertex = VertexServerGeoObject.getVertexByCode(type, code);

    if (vertex != null)
    {
      return new VertexServerGeoObject(type, vertex);
    }

    return null;
  }

  @Override
  public VertexServerGeoObject getGeoObjectByUid(String uid)
  {
    VertexObject vertex = VertexServerGeoObject.getVertex(type, uid);

    if (vertex != null)
    {
      return new VertexServerGeoObject(type, vertex);
    }

    return null;
  }

  @Override
  public VertexServerGeoObject newInstance()
  {
    VertexObject vertex = VertexServerGeoObject.newInstance(type);

    return new VertexServerGeoObject(type, vertex);
  }

  @Override
  public ServerGeoObjectQuery createQuery(Date date)
  {
    return new VertexGeoObjectQuery(type, date);
  }
}
