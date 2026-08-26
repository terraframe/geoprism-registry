package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class RemoveObjectEdgeEvent extends AbstractRepositoryEvent implements BusinessObjectEvent
{
  private String   sourceCode;

  private TypeInfo sourceType;

  private String   targetCode;

  private TypeInfo targetType;

  private TypeInfo edgeType;

  private Date     startDate;

  private Date     endDate;

  public RemoveObjectEdgeEvent()
  {
  }

  public RemoveObjectEdgeEvent(String targetCode, TypeInfo targetType, String sourceCode, TypeInfo sourceType, TypeInfo edgeType, Date startDate, Date endDate)
  {
    super(UUID.randomUUID().toString());
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeType = edgeType;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public String getSourceCode()
  {
    return sourceCode;
  }

  public void setSourceCode(String sourceCode)
  {
    this.sourceCode = sourceCode;
  }

  public TypeInfo getSourceType()
  {
    return sourceType;
  }

  public void setSourceType(TypeInfo sourceType)
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

  public TypeInfo getTargetType()
  {
    return targetType;
  }

  public void setTargetType(TypeInfo targetType)
  {
    this.targetType = targetType;
  }

  public TypeInfo getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(TypeInfo edgeType)
  {
    this.edgeType = edgeType;
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
    return dto.getHierarchyTypes().anyMatch(this.getEdgeType().getTypeCode()::equals);
  }

}
