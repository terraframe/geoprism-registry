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
package net.geoprism.registry.visualization;

import com.google.gson.JsonElement;

public class RelationshipTypeCountMetadata
{
  private final String     code;

  private final String     edgeClass;

  private final String     relationshipType;

  private final String     layout;

  private final JsonElement label;

  public RelationshipTypeCountMetadata(
      String code,
      String edgeClass,
      String relationshipType,
      String layout,
      JsonElement label
  )
  {
    this.code = code;
    this.edgeClass = edgeClass;
    this.relationshipType = relationshipType;
    this.layout = layout;
    this.label = label;
  }

  public String getCode()
  {
    return this.code;
  }

  public String getEdgeClass()
  {
    return this.edgeClass;
  }

  public String getRelationshipType()
  {
    return this.relationshipType;
  }

  public String getLayout()
  {
    return this.layout;
  }

  public JsonElement getLabel()
  {
    return this.label;
  }
}
