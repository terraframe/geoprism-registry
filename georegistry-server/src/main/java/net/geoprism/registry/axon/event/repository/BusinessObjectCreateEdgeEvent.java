package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class BusinessObjectCreateEdgeEvent extends AbstractRepositoryEvent implements BusinessObjectEvent
{
  private String  sourceCode;

  private String  sourceType;

  private String  edgeUid;

  private String  edgeType;

  private String  targetType;

  private String  targetCode;

  private String  dataSource;

  private Boolean validate;

  public BusinessObjectCreateEdgeEvent()
  {
  }

  public BusinessObjectCreateEdgeEvent(String sourceCode, String sourceType, String edgeType, String targetCode, String targetType, String dataSource, Boolean validate)
  {
    super(UUID.randomUUID().toString());

    this.edgeUid = UUID.randomUUID().toString();
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeType = edgeType;
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.dataSource = dataSource;
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

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
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
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    if (!dto.getBusinessTypes().anyMatch(this.getSourceType()::equals))
    {
      return false;
    }

    if (!dto.getBusinessTypes().anyMatch(this.getTargetType()::equals))
    {
      return false;
    }
    
    return dto.getBusinessEdgeTypes().anyMatch(this.getEdgeType()::equals);
  }

  @Override
  public String getBaseObjectId()
  {
    return this.edgeUid;
  }
}
