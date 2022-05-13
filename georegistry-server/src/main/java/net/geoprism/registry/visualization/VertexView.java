package net.geoprism.registry.visualization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;

public class VertexView
{
  public static enum ObjectType {
      BUSINESS,
      GEOOBJECT
  }
  
  private String id;
  
  private String code;
  
  private String label;
  
  private String typeCode;
  
  private ObjectType objectType;
  
  private String relation;

  public VertexView(ObjectType objectType, String id, String code, String typeCode, String label, String relation)
  {
    super();
    this.objectType = objectType;
    this.id = id;
    this.code = code;
    this.label = label;
    this.typeCode = typeCode;
    this.relation = relation;
  }
  
  public static VertexView fromBusinessObject(BusinessObject bo, String relation)
  {
    String label = bo.getLabel();
    return new VertexView(ObjectType.BUSINESS, "g-" + bo.getCode(), bo.getCode(), bo.getType().getCode(), (label == null || label.length() == 0) ? bo.getCode() : label, relation);
  }
  
  public static VertexView fromGeoObject(ServerGeoObjectIF go, String relation)
  {
    String label = go.getDisplayLabel().getValue();
    return new VertexView(ObjectType.GEOOBJECT, "g-" + go.getUid(), go.getCode(), go.getType().getCode(), (label == null || label.length() == 0) ? go.getCode() : label, relation);
  }
  
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();

    return (JsonObject) builder.create().toJsonTree(this);
  }
  
  public static VertexView fromJSON(String sJson)
  {
    GsonBuilder builder = new GsonBuilder();

    return builder.create().fromJson(sJson, VertexView.class);
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
}
