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
package net.geoprism.registry.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.jobs.ImportError.ErrorResolution;
import net.geoprism.spring.core.JsonArrayDeserializer;
import net.geoprism.spring.core.JsonObjectDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

public class ErrorResolveDTO
{
  @NotBlank
  private String          historyId;

  @NotBlank
  private String          importErrorId;

  private ErrorResolution resolution;

  @JsonDeserialize(using = JsonArrayDeserializer.class)
  private JsonArray       parentTreeNode;

  @JsonDeserialize(using = JsonObjectDeserializer.class)
  private JsonObject      geoObject;

  @JsonProperty("isNew")
  private Boolean         isNew;

  public String getHistoryId()
  {
    return historyId;
  }

  public void setHistoryId(String historyId)
  {
    this.historyId = historyId;
  }

  public String getImportErrorId()
  {
    return importErrorId;
  }

  public void setImportErrorId(String importErrorId)
  {
    this.importErrorId = importErrorId;
  }

  public ErrorResolution getResolution()
  {
    return resolution;
  }

  public void setResolution(ErrorResolution resolution)
  {
    this.resolution = resolution;
  }

  public JsonArray getParentTreeNode()
  {
    return parentTreeNode;
  }

  public void setParentTreeNode(JsonArray parentTreeNode)
  {
    this.parentTreeNode = parentTreeNode;
  }

  public JsonObject getGeoObject()
  {
    return geoObject;
  }

  public void setGeoObject(JsonObject geoObject)
  {
    this.geoObject = geoObject;
  }

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

}
