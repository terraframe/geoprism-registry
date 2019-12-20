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

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;

public class VertexSynonymRestriction extends AbstractVertexRestriction implements VertexGeoObjectRestriction
{
  private String              label;

  private ServerGeoObjectIF   parent;

  private ServerHierarchyType hierarchyType;

  private Date                date;

  public VertexSynonymRestriction(String label, Date date)
  {
    this.label = label;
    this.date = date;
    this.parent = null;
    this.hierarchyType = null;
  }

  public VertexSynonymRestriction(String label, Date date, ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    this.label = label;
    this.date = date;
    this.parent = parent;
    this.hierarchyType = hierarchyType;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    statement.append(",where: (code = :label");
    statement.append(" OR displayLabel_cot CONTAINS (:date BETWEEN startDate AND endDate AND " + localize("value") + " = :label)");
    statement.append(" OR out('geo_vertex_has_synonym').label CONTAINS :label)");

    parameters.put("label", this.label);
    parameters.put("date", this.date);

    if (this.parent != null && this.hierarchyType != null)
    {
      MdEdgeDAOIF mdEdge = this.hierarchyType.getMdEdge();

      statement.append("}.in('" + mdEdge.getDBClassName() + "'){where: (uuid=:uuid), while: (true)");

      parameters.put("uuid", this.parent.getUid());
    }
  }
}
