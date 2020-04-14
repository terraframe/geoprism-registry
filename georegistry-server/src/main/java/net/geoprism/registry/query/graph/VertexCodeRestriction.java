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

import java.util.Map;

public class VertexCodeRestriction implements VertexGeoObjectRestriction
{
  private String code;

  public VertexCodeRestriction(String code)
  {
    this.code = code;
  }

  @Override
  public void restrict(StringBuilder statement, Map<String, Object> parameters)
  {
    statement.append(",where: (code = :code)");

    parameters.put("code", this.code);
  }
}
