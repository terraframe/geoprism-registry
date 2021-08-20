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
package net.geoprism.registry.model;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.TreeNode;

public abstract class ServerTreeNode
{
  private ServerGeoObjectIF   geoObject;

  private ServerHierarchyType hierarchyType;

  private Date                startDate;

  private Date                endDate;
  
  private String              oid;

  public ServerTreeNode(ServerGeoObjectIF geoObject, ServerHierarchyType hierarchyType, Date startDate, Date endDate, String oid)
  {
    this.geoObject = geoObject;
    this.hierarchyType = hierarchyType;
    this.startDate = startDate;
    this.endDate = endDate;
    this.oid = oid;
  }

  public ServerGeoObjectIF getGeoObject()
  {
    return geoObject;
  }

  public ServerHierarchyType getHierarchyType()
  {
    return hierarchyType;
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

  public abstract TreeNode toNode(boolean enforcePermissions);

}
