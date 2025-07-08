package net.geoprism.registry.view;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class EventPublishingConfiguration
{
  private Date                     date;

  private Date                     startDate;

  private Date                     endDate;

  public List<ServerGeoObjectType> types;

  public List<ServerHierarchyType> hierarchyTypes;

  public EventPublishingConfiguration(Date date, Date startDate, Date endDate)
  {
    this.date = date;
    this.startDate = startDate;
    this.endDate = endDate;

    this.types = new LinkedList<>();
    this.hierarchyTypes = new LinkedList<>();
  }

  public Date getDate()
  {
    return date;
  }

  public void setDate(Date date)
  {
    this.date = date;
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public void setStartDate(Date startDate)
  {
    this.startDate = startDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
  }

  public List<ServerHierarchyType> getHierarchyTypes()
  {
    return hierarchyTypes;
  }

  public void addHierarchyType(ServerHierarchyType hierarchyTypes)
  {
    this.hierarchyTypes.add(hierarchyTypes);
  }

  public List<ServerGeoObjectType> getTypes()
  {
    return types;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.types.add(type);
  }

  public JsonArray toJson()
  {
    JsonArray array = new JsonArray();

    this.types.forEach(type -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", "GeoObjectType");
      object.addProperty("code", type.getCode());
    });

    this.hierarchyTypes.forEach(type -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", "HierarchyType");
      object.addProperty("code", type.getCode());
    });

    return array;
  }

}
