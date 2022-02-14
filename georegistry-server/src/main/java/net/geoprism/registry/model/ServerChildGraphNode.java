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

public class ServerChildGraphNode extends ServerGraphNode
{
  private List<ServerChildGraphNode> children;

  public ServerChildGraphNode(ServerGeoObjectIF geoObject, GraphType graphType, Date startDate, Date endDate, String oid)
  {
    super(geoObject, graphType, startDate, endDate, oid);

    this.children = Collections.synchronizedList(new LinkedList<ServerChildGraphNode>());
  }

  @Override
  public void accept(final ServerGraphNodeVisitor visitor)
  {
    visitor.visit(this);

    this.children.forEach(child -> child.accept(visitor));
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
}
