package net.geoprism.registry.axon.command.remote;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class RemoteBusinessObjectCreateEdgeCommand implements RemoteCommand
{
  @TargetAggregateIdentifier
  private String key;

  private String commitId;

  private String sourceCode;

  private String sourceType;

  private String edgeUid;

  private String edgeType;

  private String targetType;

  private String targetCode;

  private String dataSource;

  public RemoteBusinessObjectCreateEdgeCommand(String commitId, String sourceCode, String sourceType, String edgeUid, String edgeType, String targetCode, String targetType, String dataSource)
  {
    this(commitId, sourceCode + "#" + sourceType, sourceCode, sourceType, edgeUid, edgeType, targetCode, targetType, dataSource);
  }

  public RemoteBusinessObjectCreateEdgeCommand(String commitId, String key, String sourceCode, String sourceType, String edgeUid, String edgeType, String targetCode, String targetType, String dataSource)
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
}
