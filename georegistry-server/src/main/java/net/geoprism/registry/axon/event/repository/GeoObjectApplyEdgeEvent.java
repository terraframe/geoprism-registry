package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.view.PublishDTO;

public class GeoObjectApplyEdgeEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{

  private String         sourceCode;

  private String         sourceType;

  private String         edgeUid;

  private String         edgeType;

  private String         edgeTypeCode;

  private String         targetType;

  private String         targetCode;

  private Boolean        validate;

  private Date           startDate;

  private Date           endDate;

  private String         dataSource;

  private ImportStrategy strategy;

  public GeoObjectApplyEdgeEvent()
  {
  }

  public GeoObjectApplyEdgeEvent(String sourceCode, String sourceType, String edgeType, String edgeTypeCode, String targetCode, String targetType, Date startDate, Date endDate, String dataSource, ImportStrategy strategy, Boolean validate)
  {
    super(UUID.randomUUID().toString());

    this.edgeUid = UUID.randomUUID().toString();
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeType = edgeType;
    this.edgeTypeCode = edgeTypeCode;
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.startDate = startDate;
    this.endDate = endDate;
    this.dataSource = dataSource;
    this.strategy = strategy;
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

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
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
  public Boolean isValidFor(PublishDTO dto)
  {
    Date date = dto.getDate();

    if ( ( this.getEdgeType().equals(GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE) && dto.getDagTypes().anyMatch(this.getEdgeTypeCode()::equals) ) //
        || ( this.getEdgeType().equals(GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE) && dto.getUndirectedTypes().anyMatch(this.getEdgeTypeCode()::equals) ))
    {
      return ( date.after(this.getStartDate()) && date.before(this.getEndDate()) ) || date.equals(this.getStartDate()) || date.equals(this.getEndDate());
    }

    return false;
  }

  @Override
  public String getBaseObjectId()
  {
    return this.edgeUid;
  }
}
