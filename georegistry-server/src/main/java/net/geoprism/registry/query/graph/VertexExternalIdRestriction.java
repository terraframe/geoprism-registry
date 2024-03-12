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
package net.geoprism.registry.query.graph;

import java.util.Map;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;

import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;

public class VertexExternalIdRestriction extends AbstractVertexRestriction implements VertexGeoObjectRestriction
{
  private ServerGeoObjectType type;
  private ExternalSystem system;

  private String         externalId;

  public VertexExternalIdRestriction(ServerGeoObjectType type, ExternalSystem system, String externalId)
  {
    this.type = type;    
    this.system = system;
    this.externalId = externalId;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);
    
    statement.append("( SELECT expand(in) FROM (");
    statement.append("  SELECT expand(outE('" + mdEdge.getDBClassName() + "')[id = :id]) FROM :system");
    statement.append("))");
    statement.append("WHERE @class = :class ");
    
    parameters.put("system", this.system.getRID());
    parameters.put("id", this.externalId);
    parameters.put("class", this.type.getDBClassName());
  }

}
