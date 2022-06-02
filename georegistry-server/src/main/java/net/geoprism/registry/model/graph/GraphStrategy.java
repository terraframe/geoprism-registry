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
package net.geoprism.registry.model.graph;

import java.util.Date;

import net.geoprism.registry.model.ServerGraphNode;

public interface GraphStrategy
{
  public <T extends ServerGraphNode> T getChildren(VertexServerGeoObject geoObject, Boolean recursive, Date date, String boundsWKT, Long skip, Long limit);

  public <T extends ServerGraphNode> T getParents(VertexServerGeoObject geoObject, Boolean recursive, Date date, String boundsWKT, Long skip, Long limit);

  public <T extends ServerGraphNode> T addChild(VertexServerGeoObject geoObject, VertexServerGeoObject child, Date startDate, Date endDate, boolean validate);

  public <T extends ServerGraphNode> T addParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate, boolean validate);

  public void removeParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate);
}
