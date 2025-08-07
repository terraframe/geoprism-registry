package net.geoprism.registry.axon.event.remote;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectCreateEdgeCommand;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeAndCode;
import net.geoprism.registry.view.TypeAndCode.Type;

public class RemoteBusinessObjectCreateEdgeEvent implements RemoteEvent
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

  public RemoteBusinessObjectCreateEdgeEvent()
  {
  }

  public RemoteBusinessObjectCreateEdgeEvent(String commitId, String sourceCode, String sourceType, String edgeUid, String edgeType, String targetCode, String targetType)
  {
    this(commitId, sourceCode + "#" + sourceType, sourceCode, sourceType, edgeUid, edgeType, targetCode, targetType);
  }

  public RemoteBusinessObjectCreateEdgeEvent(String commitId, String key, String sourceCode, String sourceType, String edgeUid, String edgeType, String targetCode, String targetType)
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

  @Override
  public Object toCommand()
  {
    return new RemoteBusinessObjectCreateEdgeCommand(commitId, sourceCode, sourceType, edgeUid, edgeType, targetCode, targetType);
  }

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(TypeAndCode.build(edgeType, Type.BUSINESS_EDGE));
  }

}
