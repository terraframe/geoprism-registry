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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.JsonCollectors;
import net.geoprism.registry.view.serialization.DateDeserializer;
import net.geoprism.registry.view.serialization.DateSerializer;

public class PublishDTO
{

  private String         uid;

  @NotBlank
  private String         label;

  @NotNull
  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date           date;

  @NotNull
  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date           startDate;

  @NotNull
  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date           endDate;

  private String         origin;

  private List<TypeInfo> types;

  private List<TypeInfo> exclusions;

  public PublishDTO()
  {
    this.uid = UUID.randomUUID().toString();
    this.origin = GeoprismProperties.getOrigin();
  }

  public PublishDTO(String label, Date date, Date startDate, Date endDate)
  {
    this();

    this.label = label;
    this.date = date;
    this.startDate = startDate;
    this.endDate = endDate;

    this.types = new LinkedList<>();
    this.exclusions = new LinkedList<>();
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
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

  public List<TypeInfo> getTypes()
  {
    return types;
  }

  public void setTypes(List<TypeInfo> types)
  {
    this.types = types;
  }

  public List<TypeInfo> getExclusions()
  {
    return exclusions;
  }

  public void setExclusions(List<TypeInfo> exclusions)
  {
    this.exclusions = exclusions;
  }

  public void addExclusions(TypeClass type, String... codes)
  {
    Arrays.stream(codes).map(code -> TypeInfo.build(code, type)).forEach(this.exclusions::add);
  }

  public Stream<String> asStream(TypeClass type)
  {
    return this.types.stream() //
        .filter(t -> t.getTypeClass().equals(type)) //
        .filter(t -> !this.exclusions.contains(t)) //
        .map(t -> t.getTypeCode());
  }

  public void addType(TypeClass type, String... codes)
  {
    Arrays.stream(codes).map(code -> TypeInfo.build(code, type)) //
        .filter(tac -> !this.types.contains(tac)) //
        .forEach(this.types::add);
  }

  @JsonIgnore
  public Stream<String> getGeoObjectTypes()
  {
    return this.asStream(TypeClass.GEO_OBJECT_TYPE);
  }

  public void addGeoObjectType(String... geoObjectTypes)
  {
    this.addType(TypeClass.GEO_OBJECT_TYPE, geoObjectTypes);
  }

  @JsonIgnore
  public Stream<String> getBusinessTypes()
  {
    return this.asStream(TypeClass.BUSINESS_TYPE);
  }

  public void addBusinessType(String... businessTypes)
  {
    this.addType(TypeClass.BUSINESS_TYPE, businessTypes);
  }

  @JsonIgnore
  public Stream<String> getConceptClasses()
  {
    return this.asStream(TypeClass.CONCEPT_CLASS);
  }

  public void addConceptClass(String... conceptClasses)
  {
    this.addType(TypeClass.CONCEPT_CLASS, conceptClasses);
  }

  @JsonIgnore
  public Stream<String> getHierarchyTypes()
  {
    return this.asStream(TypeClass.HIERARCHY);
  }

  public void addHierarchyType(String... hierarchyTypes)
  {
    this.addType(TypeClass.HIERARCHY, hierarchyTypes);
  }

  @JsonIgnore
  public Stream<String> getDagTypes()
  {
    return this.asStream(TypeClass.DAG);
  }

  public void addDagType(String... dagTypes)
  {
    this.addType(TypeClass.DAG, dagTypes);
  }

  @JsonIgnore
  public Stream<String> getUndirectedTypes()
  {
    return this.asStream(TypeClass.UNDIRECTED_GRAPH);
  }

  public void addUndirectedType(String... undirectedTypes)
  {
    this.addType(TypeClass.UNDIRECTED_GRAPH, undirectedTypes);
  }

  @JsonIgnore
  public Stream<String> getBusinessEdgeTypes()
  {
    return this.asStream(TypeClass.BUSINESS_EDGE);
  }

  public void addBusinessEdgeType(String... businessEdgeTypes)
  {
    this.addType(TypeClass.BUSINESS_EDGE, businessEdgeTypes);
  }

  public JsonArray toTypeJson()
  {
    return toJson(this.types);
  }

  public JsonArray toExclusionJson()
  {
    return toJson(this.exclusions);
  }

  protected JsonArray toJson(List<TypeInfo> list)
  {
    return list.stream() //
        .sorted((a, b) -> a.getTypeClass().compareTo(b.getTypeClass())) //
        .map(typeCode -> {
          JsonObject object = new JsonObject();
          object.addProperty("type", typeCode.getTypeClass().getCode());
          object.addProperty("code", typeCode.getTypeCode());

          return object;
        }).collect(JsonCollectors.toJsonArray());
  }

  public boolean hasSameTypes(PublishDTO configuration)
  {
    boolean overlaps = false;
    overlaps = overlaps || overlaps(configuration.getGeoObjectTypes(), this.getGeoObjectTypes());
    overlaps = overlaps || overlaps(configuration.getConceptClasses(), this.getConceptClasses());
    overlaps = overlaps || overlaps(configuration.getBusinessTypes(), this.getBusinessTypes());
    overlaps = overlaps || overlaps(configuration.getDagTypes(), this.getDagTypes());
    overlaps = overlaps || overlaps(configuration.getHierarchyTypes(), this.getHierarchyTypes());
    overlaps = overlaps || overlaps(configuration.getUndirectedTypes(), this.getUndirectedTypes());
    overlaps = overlaps || overlaps(configuration.getBusinessEdgeTypes(), this.getBusinessEdgeTypes());

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
