package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.spring.DateDeserializer;
import net.geoprism.registry.spring.DateSerializer;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeAndCode;

public class RemoteGeoObjectCreateEdgeEvent implements RemoteEvent
{
  @TargetAggregateIdentifier
  private String key;

  private String commitId;

  private String sourceCode;

  private String sourceType;

  private String edgeUid;

  private String edgeType;

  private String edgeTypeCode;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date   startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date   endDate;

  private String targetType;

  private String targetCode;

  private String dataSource;

  public RemoteGeoObjectCreateEdgeEvent()
  {
  }

  public RemoteGeoObjectCreateEdgeEvent(String commitId, String sourceCode, String sourceType, String edgeUid, String edgeType, String edgeTypeCode, Date startDate, Date endDate, String targetCode, String targetType, String dataSource)
  {
    this(commitId, sourceCode + "#" + sourceType, sourceCode, sourceType, edgeUid, edgeType, edgeTypeCode, startDate, endDate, targetCode, targetType, dataSource);
  }

  public RemoteGeoObjectCreateEdgeEvent(String commitId, String key, String sourceCode, String sourceType, String edgeUid, String edgeType, String edgeTypeCode, Date startDate, Date endDate, String targetCode, String targetType, String dataSource)
  {
    super();
    this.commitId = commitId;
    this.key = key;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.edgeTypeCode = edgeTypeCode;
    this.startDate = startDate;
    this.endDate = endDate;
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.dataSource = dataSource;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getCommitId()
  {
    return commitId;
  }

  public void setCommitId(String commitId)
  {
    this.commitId = commitId;
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
  public String getBaseObjectId()
  {
    return this.edgeUid;
  }

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(TypeAndCode.build(edgeTypeCode, edgeType));
  }

}
