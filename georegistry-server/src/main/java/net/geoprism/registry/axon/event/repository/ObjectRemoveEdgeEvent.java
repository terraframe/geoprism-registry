package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class ObjectRemoveEdgeEvent extends AbstractObjectEdgeEvent implements RepositoryEvent
{
  private String   sourceCode;

  private TypeInfo sourceType;

  private String   edgeUid;

  private TypeInfo edgeType;

  private TypeInfo targetType;

  private String   targetCode;

  private Date     startDate;

  private Date     endDate;

  private String   dataSource;

  public ObjectRemoveEdgeEvent()
  {
  }

  public ObjectRemoveEdgeEvent(String sourceCode, TypeInfo sourceType, TypeInfo edgeType, String targetCode, TypeInfo targetType, Date startDate, Date endDate, String dataSource)
  {
    this(UUID.randomUUID().toString(), sourceCode, sourceType, edgeType, targetCode, targetType, startDate, endDate, dataSource);
  }

  public ObjectRemoveEdgeEvent(String edgeUid, String sourceCode, TypeInfo sourceType, TypeInfo edgeType, String targetCode, TypeInfo targetType, Date startDate, Date endDate, String dataSource)
  {
    super(UUID.randomUUID().toString());

    this.edgeUid = edgeUid;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeType = edgeType;
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.startDate = startDate;
    this.endDate = endDate;
    this.dataSource = dataSource;
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
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    Date date = dto.getDate();

    // Ensure the source type is valid to be published
    if (!dto.getTypes().stream().anyMatch(this.getSourceType()::equals))
    {
      return false;
    }

    // Ensure the target type is valid to be published
    if (!dto.getTypes().stream().anyMatch(this.getTargetType()::equals))
    {
      return false;
    }

    // Ensure the edge type is valid to be published
    if (!dto.getTypes().stream().anyMatch(this.getEdgeType()::equals))
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
