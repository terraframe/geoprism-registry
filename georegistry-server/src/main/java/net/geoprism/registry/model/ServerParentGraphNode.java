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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ServerParentGraphNode extends ServerGraphNode
{
  private List<ServerParentGraphNode> parents;

  /**
   * 
   * 
   * @param geoObject
   * @param graphType
   * @param date
   *          TODO
   */
  public ServerParentGraphNode(ServerGeoObjectIF geoObject, GraphType graphType, Date startDate, Date endDate, String oid)
  {
    super(geoObject, graphType, startDate, endDate, oid);

    this.parents = Collections.synchronizedList(new LinkedList<ServerParentGraphNode>());
  }

  /**
   * Returns the parents of the {@link ServerGeoObjectIF} of this
   * {@link ServerParentGraphNode}
   * 
   * @return parents of the {@link ServerGeoObjectIF} of this
   *         {@link ServerParentGraphNode}
   */
  public List<ServerParentGraphNode> getParents()
  {
    return this.parents;
  }

  /**
   * Locates and returns parents who are of the given ServerGeoObjectIFType.
   * 
   * @param typeCode
   * @return
   */
  public List<ServerParentGraphNode> findParentOfType(String typeCode)
  {
    List<ServerParentGraphNode> ret = new ArrayList<ServerParentGraphNode>();

    if (this.parents != null)
    {
      for (ServerParentGraphNode parent : parents)
      {
        if (parent.getGeoObject().getType().getCode().equals(typeCode))
        {
          ret.add(parent);
        }
        else
        {
          List<ServerParentGraphNode> parentOfParent = parent.findParentOfType(typeCode);

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
  public void addParent(ServerParentGraphNode parents)
  {
    this.parents.add(parents);
  }

  private boolean isEndDateSame(ServerParentGraphNode oNode)
  {
    if (this.getEndDate() == null && oNode.getEndDate() == null)
    {
      return true;
    }

    if (this.getEndDate() == null && oNode.getEndDate() != null || this.getEndDate() != null && oNode.getEndDate() == null)
    {
      return false;
    }

    return this.getEndDate().equals(oNode.getEndDate());
  }

  public boolean isSame(ServerParentGraphNode oNode, ServerGeoObjectIF exclude)
  {
    if (!this.getStartDate().equals(oNode.getStartDate()) || !this.isEndDateSame(oNode))
    {
      return false;
    }

    ServerParentGraphNode root = this.getRoot(exclude);
    ServerParentGraphNode oRoot = oNode.getRoot(exclude);

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

  private ServerParentGraphNode getRoot(ServerGeoObjectIF exclude)
  {
    ServerGeoObjectIF root = this.getGeoObject();

    if (root.getCode().equals(exclude.getCode()) && this.parents.size() > 0)
    {
      return this.parents.get(0).getRoot(exclude);
    }

    return this;
  }
}
