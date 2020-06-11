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
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;

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
  public ServerChildTreeNode(ServerGeoObjectIF geoObject, ServerHierarchyType hierarchyType, Date date)
  {
    super(geoObject, hierarchyType, date);

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
    GeoObject go = this.getGeoObject().toGeoObject();
    HierarchyType ht = this.getHierarchyType() != null ? this.getHierarchyType().getType() : null;

    ChildTreeNode node = new ChildTreeNode(go, ht);
    
    SingleActorDAOIF actor = null;
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      actor = Session.getCurrentSession().getUser();
    }
    
    String orgCode = go.getType().getOrganizationCode();
    String typeCode = go.getType().getCode();

    for (ServerChildTreeNode child : this.children)
    {
      if (!enforcePermissions
          || ServiceFactory.getGeoObjectRelationshipPermissionService().canViewChild(actor, orgCode, typeCode, child.getGeoObject().getType().getCode()))
      {
        node.addChild(child.toNode(enforcePermissions));
      }
    }

    return node;
  }
}
