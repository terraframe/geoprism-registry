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
package net.geoprism.registry.io.view;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.serialization.LocalizedValueDeserializer;
import org.commongeoregistry.adapter.serialization.LocalizedValueSerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

public class ImportTypeDTO
{
  private String                code;

  private String                type;

  @JsonSerialize(using = LocalizedValueSerializer.class)
  @JsonDeserialize(using = LocalizedValueDeserializer.class)
  private LocalizedValue        label;

  private List<ImportColumnDTO> attributes;

  public ImportTypeDTO()
  {
    this.attributes = new LinkedList<>();
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public LocalizedValue getLabel()
  {
    return label;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  public List<ImportColumnDTO> getAttributes()
  {
    return attributes;
  }

  public void setAttributes(List<ImportColumnDTO> attributes)
  {
    this.attributes = attributes;
  }

  public void addAttribute(ImportColumnDTO attribute)
  {
    this.attributes.add(attribute);
  }
}
