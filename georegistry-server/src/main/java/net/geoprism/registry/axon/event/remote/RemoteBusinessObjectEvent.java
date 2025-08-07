package net.geoprism.registry.axon.event.remote;

import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectCommand;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeAndCode;
import net.geoprism.registry.view.TypeAndCode.Type;

public class RemoteBusinessObjectEvent implements RemoteEvent
{
  private String  commitId;

  private String  key;

  private String  code;

  private String  type;

  private String  object;

  public RemoteBusinessObjectEvent()
  {
  }

  public RemoteBusinessObjectEvent(String commitId, String code, String type, String object)
  {
    this(commitId, code + "#" + type, code, type, object);
  }

  public RemoteBusinessObjectEvent(String commitId, String key, String code, String type, String object)
  {
    super();

    this.key = key;
    this.commitId = commitId;
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

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
  }
  
  @Override
  public Object toCommand()
  {
    return new RemoteBusinessObjectCommand(commitId, code, type, object);        
  }
  
  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(TypeAndCode.build(type, Type.BUSINESS));
  }
}
