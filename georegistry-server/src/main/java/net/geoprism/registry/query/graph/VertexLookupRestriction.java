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
package net.geoprism.registry.query.graph;

import java.util.Date;
import java.util.Map;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;

import net.geoprism.registry.model.ServerHierarchyType;

public class VertexLookupRestriction extends AbstractVertexRestriction implements VertexGeoObjectRestriction
{
  private String              text;

  private String              parentCode;

  private ServerHierarchyType hierarchyType;

  private Date                date;
  
  public VertexLookupRestriction(String text, Date date)
  {
    this.text = text;
    this.date = date;
    this.parentCode = null;
    this.hierarchyType = null;
  }

  public VertexLookupRestriction(String text, Date date, String parentCode, ServerHierarchyType hierarchyType)
  {
    this.text = text;
    this.date = date;
    this.parentCode = parentCode;
    this.hierarchyType = hierarchyType;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    statement.append(",where: (displayLabel_cot CONTAINS (");
    
    if (this.date != null)
    {
      statement.append(":date BETWEEN startDate AND endDate AND ");
    }
    
    statement.append(localize("value") + ".toLowerCase() LIKE '%' + :text + '%') AND invalid=false)");

    if (this.date != null)
    {
      parameters.put("date", this.date);
    }

    if (text != null)
    {
      parameters.put("text", this.text.toLowerCase());
    }
    else
    {
      parameters.put("text", this.text);
    }

    if (this.parentCode != null && this.hierarchyType != null)
    {
      MdEdgeDAOIF mdEdge = this.hierarchyType.getMdEdge();

      statement.append("}.in('" + mdEdge.getDBClassName() + "'){where: (code=:code), while: ($depth < 1)");

      parameters.put("code", this.parentCode);
    }
  }

}
