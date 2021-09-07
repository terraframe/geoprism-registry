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

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import net.geoprism.registry.permission.GeoObjectPermissionServiceIF;
import net.geoprism.registry.permission.GeoObjectRelationshipPermissionServiceIF;
import net.geoprism.registry.service.ServiceFactory;

public class ServerChildTreeNode extends ServerTreeNode
{
  private List<ServerChildTreeNode> children;

  /**
   * 
   * 
   * @param date
   *          TODO
   * @param _geoObject
   * @param _hierarchyType
   */
  public ServerChildTreeNode(ServerGeoObjectIF geoObject, ServerHierarchyType hierarchyType, Date startDate, Date endDate, String oid)
  {
    super(geoObject, hierarchyType, startDate, endDate, oid);

    this.children = Collections.synchronizedList(new LinkedList<ServerChildTreeNode>());
  }

  /**
   * Returns the children of the {@link GeoObject} of this
   * {@link ServerChildTreeNode}
   * 
   * @return children of the {@link GeoObject} of this
   *         {@link ServerChildTreeNode}
   */
  public List<ServerChildTreeNode> getChildren()
  {
    return this.children;
  }

  /**
   * Add a child to the current node.
   * 
   * @param _child
   */
  public void addChild(ServerChildTreeNode child)
  {
    this.children.add(child);
  }

  public ChildTreeNode toNode(boolean enforcePermissions)
  {
    final GeoObjectRelationshipPermissionServiceIF relPermServ = ServiceFactory.getGeoObjectRelationshipPermissionService();
    final GeoObjectPermissionServiceIF goPermServ = ServiceFactory.getGeoObjectPermissionService();
    
    GeoObject go = this.getGeoObject().toGeoObject();
    HierarchyType ht = this.getHierarchyType() != null ? this.getHierarchyType().getType() : null;

    ChildTreeNode node = new ChildTreeNode(go, ht);

    String orgCode = go.getType().getOrganizationCode();

    ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());

    for (ServerChildTreeNode child : this.children)
    {
      if (!enforcePermissions ||
          (relPermServ.canViewChild(orgCode, type, child.getGeoObject().getType())
           && goPermServ.canRead(orgCode, type)))
      {
        node.addChild(child.toNode(enforcePermissions));
      }
    }

    return node;
  }

  public void removeChild(ServerChildTreeNode child)
  {
    Iterator<ServerChildTreeNode> it = this.children.iterator();
    
    while (it.hasNext())
    {
      ServerChildTreeNode myChild = it.next();
      
      if (myChild.getGeoObject().getCode().equals(child.getGeoObject().getCode()))
      {
        it.remove();
      }
    }
  }
}
