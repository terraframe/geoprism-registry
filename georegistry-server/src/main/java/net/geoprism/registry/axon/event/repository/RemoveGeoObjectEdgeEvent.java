package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class RemoveGeoObjectEdgeEvent extends AbstractRepositoryEvent implements BusinessObjectEvent
{
  private String sourceCode;

  private String sourceType;

  private String targetCode;

  private String tagetType;

  private String edgeTypeCode;

  private String edgeClassType;

  public RemoveGeoObjectEdgeEvent()
  {
  }

  public RemoveGeoObjectEdgeEvent(String targetCode, String tagetType, String edgeTypeCode, String edgeClassType)
  {
    super(UUID.randomUUID().toString());
    this.targetCode = targetCode;
    this.tagetType = tagetType;
    this.edgeTypeCode = edgeTypeCode;
    this.edgeClassType = edgeClassType;
  }

  public RemoveGeoObjectEdgeEvent(String targetCode, String tagetType, String sourceCode, String sourceType, String edgeTypeCode, String edgeClassType)
  {
    super(UUID.randomUUID().toString());
    this.targetCode = targetCode;
    this.tagetType = tagetType;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeTypeCode = edgeTypeCode;
    this.edgeClassType = edgeClassType;
  }

  public String getSourceCode()
  {
    return sourceCode;
  }

  public void setSourceCode(String sourceCode)
  {
    this.sourceCode = sourceCode;
  }

  public String getSourceType()
  {
    return sourceType;
  }

  public void setSourceType(String sourceType)
  {
    this.sourceType = sourceType;
  }

  public String getTargetCode()
  {
    return targetCode;
  }

  public void setTargetCode(String targetCode)
  {
    this.targetCode = targetCode;
  }

  public String getTagetType()
  {
    return tagetType;
  }

  public void setTagetType(String tagetType)
  {
    this.tagetType = tagetType;
  }

  public String getEdgeTypeCode()
  {
    return edgeTypeCode;
  }

  public void setEdgeTypeCode(String edgeTypeCode)
  {
    this.edgeTypeCode = edgeTypeCode;
  }

  public String getEdgeClassType()
  {
    return edgeClassType;
  }

  public void setEdgeClassType(String edgeClassType)
  {
    this.edgeClassType = edgeClassType;
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {

    return dto.getHierarchyTypes().anyMatch(this.getEdgeTypeCode()::equals);
  }

}
