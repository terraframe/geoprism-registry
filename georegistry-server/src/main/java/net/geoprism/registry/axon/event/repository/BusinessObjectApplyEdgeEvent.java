package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.view.PublishDTO;

public class BusinessObjectApplyEdgeEvent extends AbstractBusinessObjectEdgeEvent implements BusinessObjectEvent
{
  private String         sourceCode;

  private String         sourceType;

  private String         edgeUid;

  private String         edgeTypeCode;

  private String         targetType;

  private String         targetCode;

  private Boolean        validate;

  private Date           startDate;

  private Date           endDate;

  private String         dataSource;

  private ImportStrategy strategy;

  public BusinessObjectApplyEdgeEvent()
  {
  }

  public BusinessObjectApplyEdgeEvent(String sourceCode, String sourceType, String edgeTypeCode, String targetCode, String targetType, Date startDate, Date endDate, String dataSource, ImportStrategy strategy, Boolean validate)
  {
    super(UUID.randomUUID().toString());

    this.edgeUid = UUID.randomUUID().toString();
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeTypeCode = edgeTypeCode;
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.validate = validate;
    this.startDate = startDate;
    this.endDate = endDate;
    this.dataSource = dataSource;
    this.strategy = strategy;
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

  public String getEdgeTypeCode()
  {
    return edgeTypeCode;
  }

  public void setEdgeTypeCode(String edgeTypeCode)
  {
    this.edgeTypeCode = edgeTypeCode;
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

  public ImportStrategy getStrategy()
  {
    return strategy;
  }

  public void setStrategy(ImportStrategy strategy)
  {
    this.strategy = strategy;
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
    Date date = dto.getDate();

    if (! ( dto.getBusinessTypes().anyMatch(this.getSourceType()::equals) || dto.getGeoObjectTypes().anyMatch(this.getSourceType()::equals) ))
    {
      return false;
    }

    if (! ( dto.getBusinessTypes().anyMatch(this.getTargetType()::equals) || dto.getGeoObjectTypes().anyMatch(this.getTargetType()::equals) ))
    {
      return false;
    }

    if (!dto.getBusinessEdgeTypes().anyMatch(this.getEdgeTypeCode()::equals))
    {
      return false;
    }

    return ( date.after(this.getStartDate()) && date.before(this.getEndDate()) ) || date.equals(this.getStartDate()) || date.equals(this.getEndDate());
  }

  @Override
  public String getBaseObjectId()
  {
    return this.edgeUid;
  }
}
