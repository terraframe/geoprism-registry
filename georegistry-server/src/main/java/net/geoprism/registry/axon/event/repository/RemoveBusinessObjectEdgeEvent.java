package net.geoprism.registry.axon.event.repository;

import java.util.Date;
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

  private Date   startDate;

  private Date   endDate;

  public RemoveBusinessObjectEdgeEvent()
  {
  }

  public RemoveBusinessObjectEdgeEvent(String targetCode, String targetType, String sourceCode, String sourceType, String edgeTypeCode, Date startDate, Date endDate)
  {
    super(UUID.randomUUID().toString());
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeTypeCode = edgeTypeCode;
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

    return dto.getHierarchyTypes().anyMatch(this.getEdgeTypeCode()::equals);
  }

}
