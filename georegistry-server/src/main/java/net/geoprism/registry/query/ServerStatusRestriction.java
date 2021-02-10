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
import java.util.List;

import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.query.graph.VertexGeoObjectRestriction;
import net.geoprism.registry.query.graph.VertexStatusRestriction;

public class ServerStatusRestriction implements ServerGeoObjectRestriction
{
  private Date date;
  
  private JoinOp joinOp;
  
  private List<GeoObjectStatus> statuses;
  
  public static enum JoinOp
  {
    AND,
    OR;
  }

  public ServerStatusRestriction(List<GeoObjectStatus> statuses, Date date, JoinOp joinOp)
  {
    this.date = date;
    this.statuses = statuses;
    this.joinOp = joinOp;
  }

  @Override
  public VertexGeoObjectRestriction create(VertexGeoObjectQuery query)
  {
    return new VertexStatusRestriction(this.statuses, date, joinOp);
  }
}
