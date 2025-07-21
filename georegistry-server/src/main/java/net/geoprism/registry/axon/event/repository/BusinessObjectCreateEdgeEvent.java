package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class BusinessObjectCreateEdgeEvent implements BusinessObjectEvent
{
  private String  sourceCode;

  private String  sourceType;

  private String  edgeUid;

  private String  edgeType;

  private String  targetType;

  private String  targetCode;

  private Boolean validate;

  public BusinessObjectCreateEdgeEvent()
  {
  }

  public BusinessObjectCreateEdgeEvent(String sourceCode, String sourceType, String edgeType, String targetType, String targetCode, Boolean validate)
  {
    super();
    this.edgeUid = UUID.randomUUID().toString();
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeType = edgeType;
    this.targetType = targetType;
    this.targetCode = targetCode;
    this.validate = validate;
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

  public String getEdgeUid()
  {
    return edgeUid;
  }

  public void setEdgeUid(String edgeUid)
  {
    this.edgeUid = edgeUid;
  }

  public String getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(String edgeType)
  {
    this.edgeType = edgeType;
  }

  public String getTargetType()
  {
    return targetType;
  }

  public void setTargetType(String targetType)
  {
    this.targetType = targetType;
  }

  public String getTargetCode()
  {
    return targetCode;
  }

  public void setTargetCode(String targetCode)
  {
    this.targetCode = targetCode;
  }

  public Boolean getValidate()
  {
    return validate;
  }

  public void setValidate(Boolean validate)
  {
    this.validate = validate;
  }

  @Override
  @JsonIgnore
  public EventType getEventType()
  {
    return EventType.HIERARCHY;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getBusinessEdgeTypes().contains(this.getEdgeType());
  }

  @Override
  public String getAggregate()
  {
    return this.edgeUid;
  }
}
