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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class VertexView
{
  public static enum ObjectType {
    BUSINESS, GEOOBJECT
  }

  private String     id;

  private String     code;

  private String     label;

  private String     typeCode;

  private ObjectType objectType;

  private String     relation;

  private boolean    readable;
  
  public VertexView(ObjectType objectType, String id, String code, String typeCode, String label, String relation, boolean readable)
  {
    super();
    this.objectType = objectType;
    this.id = id;
    this.code = code;
    this.label = label;
    this.typeCode = typeCode;
    this.relation = relation;
    this.readable = readable;
  }

  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();
    
    return (JsonObject) builder.create().toJsonTree(this);
  }

  public ObjectType getObjectType()
  {
    return objectType;
  }

  public void setObjectType(ObjectType objectType)
  {
    this.objectType = objectType;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getTypeCode()
  {
    return typeCode;
  }

  public void setTypeCode(String typeCode)
  {
    this.typeCode = typeCode;
  }

  public String getRelation()
  {
    return relation;
  }

  public void setRelation(String relation)
  {
    this.relation = relation;
  }
  
  public boolean isReadable()
  {
    return readable;
  }
  
  public void setReadable(boolean readable)
  {
    this.readable = readable;
  }
}
