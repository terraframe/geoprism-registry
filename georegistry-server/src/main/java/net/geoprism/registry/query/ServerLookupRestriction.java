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
package net.geoprism.registry.query;

import java.util.Date;

import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectRestriction;
import net.geoprism.registry.query.graph.VertexLookupRestriction;

public class ServerLookupRestriction implements ServerGeoObjectRestriction
{
  private String              text;

  private Date                startDate;

  private String              parentCode;

  private ServerHierarchyType hierarchyType;

  public ServerLookupRestriction(String text, Date startDate)
  {
    this.text = text;
    this.startDate = startDate;
    this.parentCode = null;
    this.hierarchyType = null;
  }

  public ServerLookupRestriction(String text, Date startDate, String parentCode, ServerHierarchyType hierarchyType)
  {
    this.text = text;
    this.startDate = startDate;
    this.parentCode = parentCode;
    this.hierarchyType = hierarchyType;
  }

  @Override
  public VertexGeoObjectRestriction create(VertexGeoObjectQuery query)
  {
    return new VertexLookupRestriction(this.text, this.startDate, this.parentCode, this.hierarchyType);
  }
}
