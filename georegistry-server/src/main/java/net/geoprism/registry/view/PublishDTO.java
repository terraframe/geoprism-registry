package net.geoprism.registry.view;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.JsonCollectors;
import net.geoprism.registry.spring.DateDeserializer;
import net.geoprism.registry.spring.DateSerializer;

public class PublishDTO
{
  public static enum Type {
    GEO_OBJECT, HIERARCHY, DAG, UNDIRECTED, BUSINESS, BUSINESS_EDGE
  }

  public static class TypeCode
  {
    private String code;

    private Type   type;

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }

    public Type getType()
    {
      return type;
    }

    public void setType(Type type)
    {
      this.type = type;
    }

    public static TypeCode build(String code, Type type)
    {
      TypeCode typeCode = new TypeCode();
      typeCode.setCode(code);
      typeCode.setType(type);

      return typeCode;
    }
  }

  private String         uid;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date           date;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date           startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date           endDate;

  private String         origin;

  private List<TypeCode> types;

  private List<TypeCode> exclusions;

  public PublishDTO()
  {
    this.uid = UUID.randomUUID().toString();
    this.origin = GeoprismProperties.getOrigin();
  }

  public PublishDTO(Date date, Date startDate, Date endDate)
  {
    this();

    this.date = date;
    this.startDate = startDate;
    this.endDate = endDate;

    this.types = new LinkedList<>();
    this.exclusions = new LinkedList<>();
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public String getOrigin()
  {
    return origin;
  }

  public void setOrigin(String origin)
  {
    this.origin = origin;
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

  public List<TypeCode> getTypes()
  {
    return types;
  }

  public void setTypes(List<TypeCode> types)
  {
    this.types = types;
  }

  public List<TypeCode> getExclusions()
  {
    return exclusions;
  }

  public void setExclusions(List<TypeCode> exclusions)
  {
    this.exclusions = exclusions;
  }

  public Stream<String> asStream(Type type)
  {
    return this.types.stream().filter(t -> t.getType().equals(type)).map(t -> t.getCode());
  }

  public void addType(Type type, String... codes)
  {
    Arrays.stream(codes).map(code -> TypeCode.build(code, type)).forEach(this.types::add);
  }

  @JsonIgnore
  public Stream<String> getGeoObjectTypes()
  {
    return this.asStream(Type.GEO_OBJECT);
  }

  public void addGeoObjectType(String... geoObjectTypes)
  {
    this.addType(Type.GEO_OBJECT, geoObjectTypes);
  }

  @JsonIgnore
  public Stream<String> getHierarchyTypes()
  {
    return this.asStream(Type.HIERARCHY);
  }

  public void addHierarchyType(String... hierarchyTypes)
  {
    this.addType(Type.HIERARCHY, hierarchyTypes);
  }

  @JsonIgnore
  public Stream<String> getDagTypes()
  {
    return this.asStream(Type.DAG);
  }

  public void addDagType(String... dagTypes)
  {
    this.addType(Type.DAG, dagTypes);
  }

  @JsonIgnore
  public Stream<String> getUndirectedTypes()
  {
    return this.asStream(Type.UNDIRECTED);
  }

  public void addUndirectedType(String... undirectedTypes)
  {
    this.addType(Type.UNDIRECTED, undirectedTypes);
  }

  @JsonIgnore
  public Stream<String> getBusinessTypes()
  {
    return this.asStream(Type.BUSINESS);
  }

  public void addBusinessType(String... businessTypes)
  {
    this.addType(Type.BUSINESS, businessTypes);
  }

  @JsonIgnore
  public Stream<String> getBusinessEdgeTypes()
  {
    return this.asStream(Type.BUSINESS_EDGE);
  }

  public void addBusinessEdgeType(String... businessEdgeTypes)
  {
    this.addType(Type.BUSINESS_EDGE, businessEdgeTypes);
  }

  public JsonArray toTypeJson()
  {
    return this.types.stream() //
        .sorted((a, b) -> a.getType().compareTo(b.getType())) //
        .map(typeCode -> {
          JsonObject object = new JsonObject();
          object.addProperty("type", typeCode.getType().name());
          object.addProperty("code", typeCode.getCode());

          return object;
        }).collect(JsonCollectors.toJsonArray());

  }

  public boolean hasSameTypes(PublishDTO configuration)
  {
    boolean overlaps = false;
    overlaps = overlaps || overlaps(configuration.getBusinessEdgeTypes(), this.getBusinessEdgeTypes());
    overlaps = overlaps || overlaps(configuration.getBusinessTypes(), this.getBusinessTypes());
    overlaps = overlaps || overlaps(configuration.getDagTypes(), this.getDagTypes());
    overlaps = overlaps || overlaps(configuration.getGeoObjectTypes(), this.getGeoObjectTypes());
    overlaps = overlaps || overlaps(configuration.getHierarchyTypes(), this.getHierarchyTypes());
    overlaps = overlaps || overlaps(configuration.getUndirectedTypes(), this.getUndirectedTypes());

    return overlaps;
  }

  protected boolean overlaps(Stream<String> list, Stream<String> otherList)
  {
    List<String> list2 = otherList.toList();

    Set<String> result = list //
        .distinct() //
        .filter(list2::contains) //
        .collect(Collectors.toSet());

    return result.size() > 0;
  }

}
