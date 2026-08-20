package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class ObjectApplyEdgeEvent extends AbstractObjectEdgeEvent implements RepositoryEvent, ImportHistoryEvent
{
  private String         sourceCode;

  private TypeInfo       sourceType;

  private String         edgeUid;

  private TypeInfo       edgeType;

  private TypeInfo       targetType;

  private String         targetCode;

  private Boolean        validate;

  private Date           startDate;

  private Date           endDate;

  private String         dataSource;

  private ImportStrategy strategy;

  private String         historyId;

  public ObjectApplyEdgeEvent()
  {
  }

  public ObjectApplyEdgeEvent(String sourceCode, TypeInfo sourceType, TypeInfo edgeType, String targetCode, TypeInfo targetType, Date startDate, Date endDate, String dataSource, ImportStrategy strategy, Boolean validate)
  {
    this(sourceCode, sourceType, edgeType, targetCode, targetType, startDate, endDate, dataSource, strategy, validate, null);
  }

  public ObjectApplyEdgeEvent(String sourceCode, TypeInfo sourceType, TypeInfo edgeType, String targetCode, TypeInfo targetType, Date startDate, Date endDate, String dataSource, ImportStrategy strategy, Boolean validate, String historyId)
  {
    super(UUID.randomUUID().toString());

    this.edgeUid = UUID.randomUUID().toString();
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeType = edgeType;
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.validate = validate;
    this.startDate = startDate;
    this.endDate = endDate;
    this.dataSource = dataSource;
    this.strategy = strategy;
    this.historyId = historyId;
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

  public String getEdgeUid()
  {
    return edgeUid;
  }

  public void setEdgeUid(String edgeUid)
  {
    this.edgeUid = edgeUid;
  }

  public TypeInfo getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(TypeInfo edgeType)
  {
    this.edgeType = edgeType;
  }

  public TypeInfo getTargetType()
  {
    return targetType;
  }

  public void setTargetType(TypeInfo targetType)
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

  public String getHistoryId()
  {
    return historyId;
  }

  public void setHistoryId(String historyId)
  {
    this.historyId = historyId;
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

    // Ensure the source type is valid to be published
    if ( ( ( this.getSourceType().getTypeClass().equals(TypeClass.BUSINESS_TYPE) && !dto.getBusinessTypes().anyMatch(this.getSourceType().getTypeCode()::equals) ) //
        || ( this.getSourceType().getTypeClass().equals(TypeClass.GEO_OBJECT_TYPE) && !dto.getGeoObjectTypes().anyMatch(this.getSourceType().getTypeCode()::equals) ) //
        || ( this.getSourceType().getTypeClass().equals(TypeClass.CONCEPT_CLASS) && !dto.getConceptClasses().anyMatch(this.getSourceType().getTypeCode()::equals) ) //
    ))
    {
      return false;
    }

    // Ensure the target type is valid to be published
    if ( ( ( this.getTargetType().getTypeClass().equals(TypeClass.BUSINESS_TYPE) && !dto.getBusinessTypes().anyMatch(this.getTargetType().getTypeCode()::equals) ) //
        || ( this.getTargetType().getTypeClass().equals(TypeClass.GEO_OBJECT_TYPE) && !dto.getGeoObjectTypes().anyMatch(this.getTargetType().getTypeCode()::equals) ) //
        || ( this.getTargetType().getTypeClass().equals(TypeClass.CONCEPT_CLASS) && !dto.getConceptClasses().anyMatch(this.getTargetType().getTypeCode()::equals) ) //
    ))
    {
      return false;
    }

    // Ensure the edge type is valid to be published
    if ( ( ( this.getEdgeType().getTypeClass().equals(TypeClass.BUSINESS_EDGE) && !dto.getBusinessEdgeTypes().anyMatch(this.getEdgeType().getTypeCode()::equals) ) //
        || ( this.getEdgeType().getTypeClass().equals(TypeClass.CONCEPT_EDGE) && !dto.getConceptEdgeTypes().anyMatch(this.getEdgeType().getTypeCode()::equals) ) //
    ))
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
