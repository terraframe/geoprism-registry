package net.geoprism.registry.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.jobs.ImportError.ErrorResolution;
import net.geoprism.registry.spring.JsonObjectDeserializer;
import net.geoprism.spring.core.JsonArrayDeserializer;

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
