package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.view.PublishDTO;

public class BusinessObjectAddGeoObjectEvent extends AbstractRepositoryEvent implements BusinessObjectEvent
{
  private String        type;

  private String        code;

  private String        edgeUid;

  private String        edgeType;

  private String        geoObjectType;

  private String        geoObjectCode;

  private EdgeDirection direction;

  private String        dataSource;

  public BusinessObjectAddGeoObjectEvent()
  {
  }

  public BusinessObjectAddGeoObjectEvent(String code, String type, String edgeType, String geoObjectType, String geoObjectCode, EdgeDirection direction, String dataSource)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.edgeType = edgeType;
    this.edgeUid = UUID.randomUUID().toString();
    this.geoObjectType = geoObjectType;
    this.geoObjectCode = geoObjectCode;
    this.direction = direction;
    this.dataSource = dataSource;
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

  public String getEdgeUid()
  {
    return edgeUid;
  }

  public void setEdgeUid(String edgeUid)
  {
    this.edgeUid = edgeUid;
  }

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }

  @Override
  public String getBaseObjectId()
  {
    return this.code + "#" + this.type + "#" + this.edgeType + "#" + this.geoObjectCode + "#" + this.geoObjectType;
  }

  @Override
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getBusinessEdgeTypes().anyMatch(this.getEdgeType()::equals);
  }

}
