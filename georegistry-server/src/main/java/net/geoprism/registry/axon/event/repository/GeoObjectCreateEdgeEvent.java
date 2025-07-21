package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class GeoObjectCreateEdgeEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String  sourceCode;

  private String  sourceType;

  private String  edgeUid;

  private String  edgeType;

  private String  targetType;

  private String  targetCode;

  private Boolean validate;

  private Date    startDate;

  private Date    endDate;

  public GeoObjectCreateEdgeEvent()
  {
  }

  public GeoObjectCreateEdgeEvent(String sourceCode, String sourceType, String edgeType, String targetType, String targetCode, Date startDate, Date endDate, Boolean validate)
  {
    super();
    this.edgeUid = UUID.randomUUID().toString();
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeType = edgeType;
    this.targetType = targetType;
    this.targetCode = targetCode;
    this.startDate = startDate;
    this.endDate = endDate;
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
  public EventType getEventType()
  {
    return EventType.HIERARCHY;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    Date date = dto.getDate();
    
    if (dto.getDagTypes().contains(this.getEdgeType()) || dto.getUndirectedTypes().contains(this.getEdgeType()))
    {
      return ( date.after(this.getStartDate()) && date.before(this.getEndDate()) ) || date.equals(this.getStartDate()) || date.equals(this.getEndDate());
    }

    return false;
  }

  @Override
  public String getAggregate()
  {
    return this.edgeUid;
  }
}
