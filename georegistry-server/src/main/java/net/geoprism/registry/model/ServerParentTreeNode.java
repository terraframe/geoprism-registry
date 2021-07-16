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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import net.geoprism.registry.service.ServiceFactory;

public class ServerParentTreeNode extends ServerTreeNode
{
  private List<ServerParentTreeNode> parents;

  /**
   * 
   * 
   * @param geoObject
   * @param hierarchyType
   * @param date
   *          TODO
   */
  public ServerParentTreeNode(ServerGeoObjectIF geoObject, ServerHierarchyType hierarchyType, Date startDate, Date endDate, String oid)
  {
    super(geoObject, hierarchyType, startDate, endDate, oid);

    this.parents = Collections.synchronizedList(new LinkedList<ServerParentTreeNode>());
  }

  /**
   * Returns the parents of the {@link ServerGeoObjectIF} of this
   * {@link ServerParentTreeNode}
   * 
   * @return parents of the {@link ServerGeoObjectIF} of this
   *         {@link ServerParentTreeNode}
   */
  public List<ServerParentTreeNode> getParents()
  {
    return this.parents;
  }

  /**
   * Locates and returns parents who are of the given ServerGeoObjectIFType.
   * 
   * @param typeCode
   * @return
   */
  public List<ServerParentTreeNode> findParentOfType(String typeCode)
  {
    List<ServerParentTreeNode> ret = new ArrayList<ServerParentTreeNode>();

    if (this.parents != null)
    {
      for (ServerParentTreeNode parent : parents)
      {
        if (parent.getGeoObject().getType().getCode().equals(typeCode))
        {
          ret.add(parent);
        }
        else
        {
          List<ServerParentTreeNode> parentOfParent = parent.findParentOfType(typeCode);

          ret.addAll(parentOfParent);
        }
      }
    }

    return ret;
  }

  /**
   * Add a parent to the current node.
   * 
   * @param parents
   */
  public void addParent(ServerParentTreeNode parents)
  {
    this.parents.add(parents);
  }

  public ParentTreeNode toNode(boolean enforcePermissions)
  {
    GeoObject geoObject = this.getGeoObject().toGeoObject();
    HierarchyType ht = this.getHierarchyType() != null ? this.getHierarchyType().getType() : null;

    ParentTreeNode node = new ParentTreeNode(geoObject, ht);

    String orgCode = geoObject.getType().getOrganizationCode();
    ServerGeoObjectType type = ServerGeoObjectType.get(geoObject.getType());

    for (ServerParentTreeNode parent : this.parents)
    {
      if (!enforcePermissions || ServiceFactory.getGeoObjectRelationshipPermissionService().canViewChild(orgCode, parent.getGeoObject().getType(), type))
      {
        node.addParent(parent.toNode(enforcePermissions));
      }
    }

    return node;
  }
  
  private boolean isEndDateSame(ServerParentTreeNode oNode)
  {
    if (this.getEndDate() == null && oNode.getEndDate() == null)
    {
      return true;
    }
    
    if (this.getEndDate() == null && oNode.getEndDate() != null
        || this.getEndDate() != null && oNode.getEndDate() == null)
    {
      return false;
    }
    
    return this.getEndDate().equals(oNode.getEndDate());
  }

  public boolean isSame(ServerParentTreeNode oNode, ServerGeoObjectIF exclude)
  {
    if (!this.getStartDate().equals(oNode.getStartDate()) || !this.isEndDateSame(oNode))
    {
      return false;
    }

    ServerParentTreeNode root = this.getRoot(exclude);
    ServerParentTreeNode oRoot = oNode.getRoot(exclude);

    if (root != null && oRoot != null)
    {
      String code = root.getGeoObject().getCode();
      String oCode = oRoot.getGeoObject().getCode();

      if (!code.equals(oCode))
      {
        return false;
      }
    }
    else if (root == null && oRoot != null)
    {
      return false;
    }
    else if (root != null && oRoot == null)
    {
      return false;
    }

    return true;
  }

  private ServerParentTreeNode getRoot(ServerGeoObjectIF exclude)
  {
    ServerGeoObjectIF root = this.getGeoObject();

    if (root.getCode().equals(exclude.getCode()) && this.parents.size() > 0)
    {
      return this.parents.get(0).getRoot(exclude);
    }

    return this;
  }
}
