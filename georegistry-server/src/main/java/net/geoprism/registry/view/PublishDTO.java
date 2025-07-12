package net.geoprism.registry.view;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PublishDTO
{
  private String       uid;

  private Date         date;

  private Date         startDate;

  private Date         endDate;

  private List<String> geoObjectTypes;

  private List<String> hierarchyTypes;

  private List<String> businessTypes;

  private List<String> businessEdgeTypes;

  public PublishDTO()
  {
    this.uid = UUID.randomUUID().toString();
  }

  public PublishDTO(Date date, Date startDate, Date endDate)
  {
    this.date = date;
    this.startDate = startDate;
    this.endDate = endDate;

    this.geoObjectTypes = new LinkedList<>();
    this.hierarchyTypes = new LinkedList<>();
    this.businessTypes = new LinkedList<>();
    this.businessEdgeTypes = new LinkedList<>();
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
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

  public List<String> getGeoObjectTypes()
  {
    return geoObjectTypes;
  }

  public void setGeoObjectTypes(List<String> geoObjectTypes)
  {
    this.geoObjectTypes = geoObjectTypes;
  }

  public void addGeoObjectType(String geoObjectType)
  {
    this.geoObjectTypes.add(geoObjectType);
  }

  public List<String> getHierarchyTypes()
  {
    return hierarchyTypes;
  }

  public void setHierarchyTypes(List<String> hierarchyTypes)
  {
    this.hierarchyTypes = hierarchyTypes;
  }

  public void addHierarchyType(String hierarchyType)
  {
    this.hierarchyTypes.add(hierarchyType);
  }

  public List<String> getBusinessTypes()
  {
    return businessTypes;
  }

  public void setBusinessTypes(List<String> businessTypes)
  {
    this.businessTypes = businessTypes;
  }

  public void addBusinessType(String businessType)
  {
    this.businessTypes.add(businessType);
  }

  public List<String> getBusinessEdgeTypes()
  {
    return businessEdgeTypes;
  }

  public void setBusinessEdgeTypes(List<String> businessEdgeTypes)
  {
    this.businessEdgeTypes = businessEdgeTypes;
  }

  public void addBusinessEdgeType(String businessEdgeType)
  {
    this.businessEdgeTypes.add(businessEdgeType);
  }

  public JsonArray toJson()
  {
    JsonArray array = new JsonArray();

    this.geoObjectTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", "GeoObjectType");
      object.addProperty("code", code);
    });

    this.hierarchyTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", "HierarchyType");
      object.addProperty("code", code);
    });

    this.businessTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", "BusinessType");
      object.addProperty("code", code);
    });

    this.businessEdgeTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", "BusinessEdgeType");
      object.addProperty("code", code);
    });

    return array;
  }

}
