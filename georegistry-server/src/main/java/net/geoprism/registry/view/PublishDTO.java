package net.geoprism.registry.view;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.spring.DateDeserializer;
import net.geoprism.registry.spring.DateSerializer;

public class PublishDTO
{
  private String       uid;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date         date;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date         startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date         endDate;

  private List<String> geoObjectTypes;

  private List<String> hierarchyTypes;

  private List<String> dagTypes;

  private List<String> undirectedTypes;

  private List<String> businessTypes;

  private List<String> businessEdgeTypes;

  public PublishDTO()
  {
    this.uid = UUID.randomUUID().toString();
  }

  public PublishDTO(Date date, Date startDate, Date endDate)
  {
    this();

    this.date = date;
    this.startDate = startDate;
    this.endDate = endDate;

    this.geoObjectTypes = new LinkedList<>();
    this.hierarchyTypes = new LinkedList<>();
    this.dagTypes = new LinkedList<>();
    this.undirectedTypes = new LinkedList<>();
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

  public void addGeoObjectType(String... geoObjectTypes)
  {
    Arrays.stream(geoObjectTypes).forEach(geoObjectType -> {
      this.geoObjectTypes.add(geoObjectType);
    });
  }

  public List<String> getHierarchyTypes()
  {
    return hierarchyTypes;
  }

  public void setHierarchyTypes(List<String> hierarchyTypes)
  {
    this.hierarchyTypes = hierarchyTypes;
  }

  public void addHierarchyType(String... hierarchyTypes)
  {
    Arrays.stream(hierarchyTypes).forEach(type -> {
      this.hierarchyTypes.add(type);
    });
  }

  public List<String> getDagTypes()
  {
    return dagTypes;
  }

  public void setDagTypes(List<String> dagTypes)
  {
    this.dagTypes = dagTypes;
  }

  public void addDagType(String... dagTypes)
  {
    Arrays.stream(dagTypes).forEach(type -> {
      this.dagTypes.add(type);
    });
  }

  public List<String> getUndirectedTypes()
  {
    return this.undirectedTypes;
  }

  public void setUndirectedTypes(List<String> undirectedTypes)
  {
    this.undirectedTypes = undirectedTypes;
  }

  public void addUndirectedType(String... undirectedTypes)
  {
    Arrays.stream(undirectedTypes).forEach(type -> {
      this.undirectedTypes.add(type);
    });
  }

  public List<String> getBusinessTypes()
  {
    return businessTypes;
  }

  public void setBusinessTypes(List<String> businessTypes)
  {
    this.businessTypes = businessTypes;
  }

  public void addBusinessType(String... businessType)
  {
    Arrays.stream(businessType).forEach(type -> {
      this.businessTypes.add(type);
    });
  }

  public List<String> getBusinessEdgeTypes()
  {
    return businessEdgeTypes;
  }

  public void setBusinessEdgeTypes(List<String> businessEdgeTypes)
  {
    this.businessEdgeTypes = businessEdgeTypes;
  }

  public void addBusinessEdgeType(String... businessEdgeTypes)
  {
    Arrays.stream(businessEdgeTypes).forEach(type -> {
      this.businessEdgeTypes.add(type);
    });
  }

  public JsonArray toJson()
  {
    JsonArray array = new JsonArray();

    this.geoObjectTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", GeoObjectType.class.getSimpleName());
      object.addProperty("code", code);

      array.add(object);
    });

    this.hierarchyTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", HierarchyType.class.getSimpleName());
      object.addProperty("code", code);

      array.add(object);
    });

    this.dagTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", DirectedAcyclicGraphType.class.getSimpleName());
      object.addProperty("code", code);

      array.add(object);
    });

    this.undirectedTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", UndirectedGraphType.class.getSimpleName());
      object.addProperty("code", code);

      array.add(object);
    });

    this.businessTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", BusinessType.class.getSimpleName());
      object.addProperty("code", code);

      array.add(object);
    });

    this.businessEdgeTypes.forEach(code -> {
      JsonObject object = new JsonObject();
      object.addProperty("type", BusinessEdgeType.class.getSimpleName());
      object.addProperty("code", code);

      array.add(object);
    });

    return array;
  }

}
