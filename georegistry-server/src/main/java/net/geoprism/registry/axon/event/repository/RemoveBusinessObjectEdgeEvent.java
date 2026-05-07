package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class RemoveBusinessObjectEdgeEvent extends AbstractRepositoryEvent implements BusinessObjectEvent
{
  private String sourceCode;

  private String sourceType;

  private String targetCode;

  private String targetType;

  private String edgeTypeCode;

  public RemoveBusinessObjectEdgeEvent()
  {
  }

  public RemoveBusinessObjectEdgeEvent(String targetCode, String targetType, String sourceCode, String sourceType, String edgeTypeCode)
  {
    super(UUID.randomUUID().toString());
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeTypeCode = edgeTypeCode;
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

  public String getTargetType()
  {
    return targetType;
  }

  public void setTargetType(String targetType)
  {
    this.targetType = targetType;
  }

  public String getEdgeTypeCode()
  {
    return edgeTypeCode;
  }

  public void setEdgeTypeCode(String edgeTypeCode)
  {
    this.edgeTypeCode = edgeTypeCode;
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
