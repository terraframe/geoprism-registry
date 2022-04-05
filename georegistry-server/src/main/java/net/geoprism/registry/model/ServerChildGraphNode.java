/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.model;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.dataaccess.TreeNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.conversion.ServerGeoObjectStrategyIF;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.service.ServiceFactory;

public class ServerChildGraphNode extends ServerGraphNode
{
  private List<ServerChildGraphNode> children;

  public ServerChildGraphNode(ServerGeoObjectIF geoObject, GraphType graphType, Date startDate, Date endDate, String oid)
  {
    super(geoObject, graphType, startDate, endDate, oid);

    this.children = Collections.synchronizedList(new LinkedList<ServerChildGraphNode>());
  }

  public List<ServerChildGraphNode> getChildren()
  {
    return this.children;
  }

  public void addChild(ServerChildGraphNode child)
  {
    this.children.add(child);
  }

  public void removeChild(ServerChildGraphNode child)
  {
    Iterator<ServerChildGraphNode> it = this.children.iterator();

    while (it.hasNext())
    {
      ServerChildGraphNode myChild = it.next();

      if (myChild.getGeoObject().getCode().equals(child.getGeoObject().getCode()))
      {
        it.remove();
      }
    }
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject json = super.toJSON();

    JsonArray jaChildren = new JsonArray();
    for (int i = 0; i < this.children.size(); ++i)
    {
      ServerChildGraphNode child = this.children.get(i);

      jaChildren.add(child.toJSON());
    }

    json.add(ChildTreeNode.JSON_CHILDREN, jaChildren);

    return json;
  }
  
  public static ServerChildGraphNode fromJSON(JsonObject jo)
  {
    ServerGeoObjectIF goif = null;
    
    if (jo.has(TreeNode.JSON_GEO_OBJECT))
    {
      GeoObject go = GeoObject.fromJSON(ServiceFactory.getAdapter(), jo.get(TreeNode.JSON_GEO_OBJECT).toString());
      
      ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());
      ServerGeoObjectStrategyIF strategy = new ServerGeoObjectService().getStrategy(type);
      goif = strategy.constructFromGeoObject(go, false);
    }
    
    GraphType graphType = null;
    
    if (jo.has("graphType"))
    {
      String graphCode = jo.get("graphType").getAsString();
      String graphTypeClass = jo.get("graphTypeClass").getAsString();
      
      graphType = GraphType.getByCode(graphTypeClass, graphCode);
    }
    
    Date startDate = null;
    
    if (jo.has("startDate"))
    {
      startDate = GeoRegistryUtil.parseDate(jo.get("startDate").getAsString());
    }
    
    Date endDate = null;
    
    if (jo.has("endDate"))
    {
      endDate = GeoRegistryUtil.parseDate(jo.get("startDate").getAsString());
    }
    
    String oid = null;
    
    if (jo.has("oid"))
    {
      oid = jo.get("oid").getAsString();
    }
    
    ServerChildGraphNode node = new ServerChildGraphNode(goif, graphType, startDate, endDate, oid);
    
    if (jo.has(ChildTreeNode.JSON_CHILDREN))
    {
      JsonArray jaChildren = jo.get(ChildTreeNode.JSON_CHILDREN).getAsJsonArray();
      
      for (int i = 0; i < jaChildren.size(); ++i)
      {
        node.addChild(ServerChildGraphNode.fromJSON(jaChildren.get(i).getAsJsonObject()));
      }
    }
    
    return node;
  }

}
