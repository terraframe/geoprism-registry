/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.model.graph.GraphStrategy;

public interface GraphType
{
  public MdEdgeDAOIF getMdEdgeDAO();
  
  public GraphStrategy getStrategy();

  public String getCode();
  
  public LocalizedValue getLabel();

  public static GraphType getByCode(String relationshipType, String code)
  {
    if (relationshipType != null)
    {
      if (relationshipType.equals("UndirectedGraphType") || relationshipType.equals(UndirectedGraphType.CLASS))
      {
        return UndirectedGraphType.getByCode(code);
      }
      else if (relationshipType.equals("DirectedAcyclicGraphType") || relationshipType.equals(DirectedAcyclicGraphType.CLASS))
      {
        return DirectedAcyclicGraphType.getByCode(code);
      }
      else
      {
        return (GraphType) com.runwaysdk.business.Business.get(relationshipType, code);
      }
    }

    return ServerHierarchyType.get(code);
  }
}
