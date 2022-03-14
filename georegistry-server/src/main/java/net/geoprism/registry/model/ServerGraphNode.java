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

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.TreeNode;

import com.google.gson.JsonObject;

public abstract class ServerGraphNode
{
  private ServerGeoObjectIF geoObject;

  private GraphType         graphType;

  private Date              startDate;

  private Date              endDate;

  private String            oid;

  public ServerGraphNode(ServerGeoObjectIF geoObject, GraphType graphType, Date startDate, Date endDate, String oid)
  {
    this.geoObject = geoObject;
    this.graphType = graphType;
    this.startDate = startDate;
    this.endDate = endDate;
    this.oid = oid;
  }

  public ServerGeoObjectIF getGeoObject()
  {
    return geoObject;
  }

  public GraphType getHierarchyType()
  {
    return graphType;
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public void setStartDate(Date startDate)
  {
    this.startDate = startDate;
  }

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public JsonObject toJSON()
  {
    JsonObject json = new JsonObject();
    
    if (this.geoObject != null)
    {
      json.add(TreeNode.JSON_GEO_OBJECT, this.geoObject.toGeoObject(this.startDate).toJSON());
    }
    else
    {
      json.add(TreeNode.JSON_GEO_OBJECT, null);
    }
    
    if (this.graphType != null) // The hierarchyType is null on the root node
    {
      json.addProperty("graphType", this.graphType.getCode());
    }
    
    return json;
  }

}
