package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.view.serialization.DateDeserializer;
import net.geoprism.registry.view.serialization.DateSerializer;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class RemoteGeoObjectCreateEdgeEvent implements RemoteEvent
{
  @TargetAggregateIdentifier
  private String   key;

  private String   commitId;

  private String   sourceCode;

  private TypeInfo sourceType;

  private String   edgeUid;

  private TypeInfo edgeType;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date     startDate;

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date     endDate;

  private TypeInfo targetType;

  private String   targetCode;

  private String   dataSource;

  public RemoteGeoObjectCreateEdgeEvent()
  {
  }

  public RemoteGeoObjectCreateEdgeEvent(String commitId, String sourceCode, TypeInfo sourceType, String edgeUid, TypeInfo edgeType, Date startDate, Date endDate, String targetCode, TypeInfo targetType, String dataSource)
  {
    this(commitId, sourceCode + "#" + sourceType, sourceCode, sourceType, edgeUid, edgeType, startDate, endDate, targetCode, targetType, dataSource);
  }

  public RemoteGeoObjectCreateEdgeEvent(String commitId, String key, String sourceCode, TypeInfo sourceType, String edgeUid, TypeInfo edgeType, Date startDate, Date endDate, String targetCode, TypeInfo targetType, String dataSource)
  {
    super();
    this.commitId = commitId;
    this.key = key;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
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

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    return this.edgeUid;
  }

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(edgeType);
  }

}
