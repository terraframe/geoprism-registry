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
package net.geoprism.registry.service;

import java.util.Date;
import java.util.List;

import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.conversion.CompositeGeoObjectStrategy;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerGeoObjectStrategyIF;
import net.geoprism.registry.conversion.TreeGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.view.GeoObjectSplitView;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.runwaysdk.business.Business;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;

public class ServerGeoObjectService extends LocalizedValueConverter
{
  @Transaction
  public ServerGeoObjectIF apply(GeoObject object, boolean isNew, boolean isImport)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(object.getType());
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    ServerGeoObjectIF geoObject = strategy.constructFromGeoObject(object, isNew);

    if (!isNew)
    {
      geoObject.lock();
    }

    geoObject.populate(object);
    geoObject.apply(isImport);

    // Return the refreshed copy of the geoObject
    return this.build(type, geoObject.getRunwayId());
  }

  @Transaction
  public ServerGeoObjectIF apply(GeoObjectOverTime goTime, boolean isNew, boolean isImport)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(goTime.getType());
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    ServerGeoObjectIF goServer = strategy.constructFromGeoObjectOverTime(goTime, isNew);

    if (!isNew)
    {
      goServer.lock();
    }

    goServer.populate(goTime);
    goServer.apply(isImport);

    // Return the refreshed copy of the geoObject
    return this.build(type, goServer.getRunwayId());
  }

  @Transaction
  public ServerGeoObjectIF split(GeoObjectSplitView view)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(view.getTypeCode());
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    final ServerGeoObjectIF source = strategy.getGeoObjectByCode(view.getSourceCode());
    source.setDate(view.getDate());

    ServerGeoObjectIF target = strategy.newInstance();
    target.setDate(view.getDate());
    target.populate(source.toGeoObject());
    target.setCode(view.getTargetCode());
    target.setDisplayLabel(view.getLabel());
    target.apply(false);

    final ServerParentTreeNode sNode = source.getParentGeoObjects(null, false);

    final List<ServerParentTreeNode> sParents = sNode.getParents();

    for (ServerParentTreeNode sParent : sParents)
    {
      final ServerGeoObjectIF parent = sParent.getGeoObject();
      final ServerHierarchyType hierarchyType = sParent.getHierarchyType();

      target.addParent(parent, hierarchyType, view.getDate(), null);
    }

    return target;
  }

  public ServerGeoObjectIF newInstance(ServerGeoObjectType type)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.newInstance();
  }

  public ServerGeoObjectIF getGeoObject(GeoObject go)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.constructFromGeoObject(go, false);
  }

  public ServerGeoObjectIF getGeoObject(GeoObjectOverTime timeGO)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(timeGO.getType());

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.constructFromGeoObjectOverTime(timeGO, false);
  }

  public ServerGeoObjectIF getGeoObjectByCode(String code, String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);
    
    if (type == null)
    {
      DataNotFoundException ex = new DataNotFoundException();
      ex.setDataIdentifier(typeCode);
      throw ex;
    }
    
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.getGeoObjectByCode(code);
  }

  public ServerGeoObjectIF getGeoObjectByCode(String code, ServerGeoObjectType type)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);

    return strategy.getGeoObjectByCode(code);
  }

  public ServerGeoObjectIF getGeoObject(String uid, String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(uid, type);
    Business business = Business.get(runwayId);

    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    return strategy.constructFromDB(business);
  }

  public ServerGeoObjectIF getGeoObjectByEntityId(String entityId)
  {
    GeoEntity entity = GeoEntity.get(entityId);

    ServerGeoObjectType type = ServerGeoObjectType.get(entity.getUniversal());

    return this.build(type, entity);
  }

  public ServerGeoObjectStrategyIF getStrategy(ServerGeoObjectType type)
  {
// Heads up: clean up
//    if (type.isLeaf())
//    {
//      return new CompositeGeoObjectStrategy(new LeafGeoObjectStrategy(type));
//    }

    return new CompositeGeoObjectStrategy(new TreeGeoObjectStrategy(type));
  }

  public ServerGeoObjectIF build(GeoEntity entity)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(entity.getUniversal());

    return this.build(type, entity);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, String runwayId)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    Business business = Business.get(runwayId);

    return strategy.constructFromDB(business);
  }

  public ServerGeoObjectIF build(ServerGeoObjectType type, Object dbObject)
  {
    ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    return strategy.constructFromDB(dbObject);
  }

  public ServerGeoObjectQuery createQuery(ServerGeoObjectType type, Date date)
  {
    return new VertexGeoObjectQuery(type, date);
    // ServerGeoObjectStrategyIF strategy = this.getStrategy(type);
    // return strategy.createQuery(date);
  }

}
