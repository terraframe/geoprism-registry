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

import net.geoprism.registry.model.CompositeServerGeoObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.ServerGeoObjectQuery;

/**
 * Strategy used for creating CompositeServerGeoObjects
 * 
 * @author terraframe
 */
public class CompositeGeoObjectStrategy implements ServerGeoObjectStrategyIF
{
  private ServerGeoObjectStrategyIF bConverter;

  private VertexGeoObjectStrategy   vConverter;

  public CompositeGeoObjectStrategy(ServerGeoObjectStrategyIF converter)
  {
    this.bConverter = converter;
    this.vConverter = new VertexGeoObjectStrategy(converter.getType());
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.bConverter.getType();
  }

  @Override
  public ServerGeoObjectIF constructFromGeoObject(GeoObject geoObject, boolean isNew)
  {
    ServerGeoObjectIF business = this.bConverter.constructFromGeoObject(geoObject, isNew);
    VertexServerGeoObject vertex = this.vConverter.constructFromGeoObject(geoObject, isNew);

    return new CompositeServerGeoObject(business, vertex);
  }
  
  @Override
  public ServerGeoObjectIF constructFromGeoObjectOverTime(GeoObjectOverTime goTime, boolean isNew)
  {
    ServerGeoObjectIF business = this.bConverter.constructFromGeoObjectOverTime(goTime, isNew);
    VertexServerGeoObject vertex = this.vConverter.constructFromGeoObjectOverTime(goTime, isNew);

    return new CompositeServerGeoObject(business, vertex);
  }

  @Override
  public ServerGeoObjectIF constructFromDB(Object dbObject)
  {
    ServerGeoObjectIF business = this.bConverter.constructFromDB(dbObject);

    VertexObject dbVertex = VertexServerGeoObject.getVertex(this.getType(), business.getUid());

    VertexServerGeoObject vertex = this.vConverter.constructFromDB(dbVertex);

    return new CompositeServerGeoObject(business, vertex);
  }

  @Override
  public ServerGeoObjectIF getGeoObjectByCode(String code)
  {
    ServerGeoObjectIF business = this.bConverter.getGeoObjectByCode(code);

    if (business != null)
    {
      VertexServerGeoObject vertex = this.vConverter.getGeoObjectByCode(code);

      return new CompositeServerGeoObject(business, vertex);
    }

    return null;
  }

  @Override
  public ServerGeoObjectIF newInstance()
  {
    ServerGeoObjectIF business = this.bConverter.newInstance();
    VertexServerGeoObject vertex = this.vConverter.newInstance();

    return new CompositeServerGeoObject(business, vertex);
  }

  @Override
  public ServerGeoObjectQuery createQuery(Date date)
  {
    return this.bConverter.createQuery(date);
  }

}
