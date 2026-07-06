package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class RemoteBusinessObjectApplyEdgeEvent implements RemoteEvent
{
  private String key;

  private String commitId;

  private String sourceCode;

  private String sourceType;

  private String edgeUid;

  private String edgeType;

  private String targetType;

  private String targetCode;

  private Date   startDate;

  private Date   endDate;

  private String dataSource;

  public RemoteBusinessObjectApplyEdgeEvent()
  {
  }

  public RemoteBusinessObjectApplyEdgeEvent(String commitId, String sourceCode, String sourceType, String edgeUid, String edgeType, String targetCode, String targetType, Date startDate, Date endDate, String dataSource)
  {
    this(commitId, sourceCode + "#" + sourceType, sourceCode, sourceType, edgeUid, edgeType, targetCode, targetType, startDate, endDate, dataSource);
  }

  public RemoteBusinessObjectApplyEdgeEvent(String commitId, String key, String sourceCode, String sourceType, String edgeUid, String edgeType, String targetCode, String targetType, Date startDate, Date endDate, String dataSource)
  {
    super();
    this.commitId = commitId;
    this.key = key;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
    this.targetCode = targetCode;
    this.targetType = targetType;
    this.startDate = startDate;
    this.endDate = endDate;
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
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(TypeInfo.build(edgeType, TypeClass.BUSINESS_EDGE));
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    return this.edgeUid;
  }
}
