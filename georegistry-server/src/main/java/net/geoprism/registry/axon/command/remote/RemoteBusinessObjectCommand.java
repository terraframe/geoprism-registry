package net.geoprism.registry.axon.command.remote;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class RemoteBusinessObjectCommand implements RemoteCommand
{
  @TargetAggregateIdentifier
  private String key;

  private String commitId;

  private String code;

  private String type;

  private String object;

  public RemoteBusinessObjectCommand(String commitId, String code, String type, String object)
  {
    super();
    this.commitId = commitId;
    this.key = code + "#" + type;
    this.code = code;
    this.type = type;
    this.object = object;
  }

  public String getCommitId()
  {
    return commitId;
  }

  public void setCommitId(String commitId)
  {
    this.commitId = commitId;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
  }

}
