package net.geoprism.registry.axon.event;

import net.geoprism.registry.model.EdgeDirection;

public class BusinessObjectAddGeoObjectEvent implements BusinessObjectEvent
{
  private String        key;

  private String        type;

  private String        code;

  private String        edgeType;

  private String        geoObjectType;

  private String        geoObjectCode;

  private EdgeDirection direction;

  public BusinessObjectAddGeoObjectEvent()
  {
  }

  public BusinessObjectAddGeoObjectEvent(String type, String code, String edgeType, String geoObjectType, String geoObjectCode, EdgeDirection direction)
  {
    this(code + "#" + type, type, code, edgeType, geoObjectType, geoObjectCode, direction);
  }

  public BusinessObjectAddGeoObjectEvent(String key, String type, String code, String edgeType, String geoObjectType, String geoObjectCode, EdgeDirection direction)
  {
    super();
    this.type = type;
    this.code = code;
    this.edgeType = edgeType;
    this.geoObjectType = geoObjectType;
    this.geoObjectCode = geoObjectCode;
    this.direction = direction;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(String edgeType)
  {
    this.edgeType = edgeType;
  }

  public String getGeoObjectType()
  {
    return geoObjectType;
  }

  public void setGeoObjectType(String geoObjectType)
  {
    this.geoObjectType = geoObjectType;
  }

  public String getGeoObjectCode()
  {
    return geoObjectCode;
  }

  public void setGeoObjectCode(String geoObjectCode)
  {
    this.geoObjectCode = geoObjectCode;
  }

  public EdgeDirection getDirection()
  {
    return direction;
  }

  public void setDirection(EdgeDirection direction)
  {
    this.direction = direction;
  }

}
