package net.geoprism.registry.axon.event.remote;

import net.geoprism.registry.axon.command.remote.RemoteBusinessObjectAddGeoObjectCommand;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeAndCode;
import net.geoprism.registry.view.TypeAndCode.Type;

public class RemoteBusinessObjectAddGeoObjectEvent implements RemoteEvent
{
  private String        commitId;

  private String        key;

  private String        type;

  private String        code;

  private String        edgeType;

  private String        geoObjectType;

  private String        geoObjectCode;

  private EdgeDirection direction;

  public RemoteBusinessObjectAddGeoObjectEvent()
  {
  }

  public RemoteBusinessObjectAddGeoObjectEvent(String commitId, String code, String type, String edgeType, String geoObjectType, String geoObjectCode, EdgeDirection direction)
  {
    this(commitId, code + "#" + type, code, type, edgeType, geoObjectType, geoObjectCode, direction);
  }

  public RemoteBusinessObjectAddGeoObjectEvent(String commitId, String key, String code, String type, String edgeType, String geoObjectType, String geoObjectCode, EdgeDirection direction)
  {
    super();
    this.commitId = commitId;
    this.key = key;
    this.code = code;
    this.type = type;
    this.edgeType = edgeType;
    this.geoObjectType = geoObjectType;
    this.geoObjectCode = geoObjectCode;
    this.direction = direction;
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

  public String getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(String edgeType)
  {
    this.edgeType = edgeType;
  }

  public String getGeoObjectType()
  {
    return geoObjectType;
  }

  public void setGeoObjectType(String geoObjectType)
  {
    this.geoObjectType = geoObjectType;
  }

  public String getGeoObjectCode()
  {
    return geoObjectCode;
  }

  public void setGeoObjectCode(String geoObjectCode)
  {
    this.geoObjectCode = geoObjectCode;
  }

  public EdgeDirection getDirection()
  {
    return direction;
  }

  public void setDirection(EdgeDirection direction)
  {
    this.direction = direction;
  }

  @Override
  public Object toCommand()
  {
    return new RemoteBusinessObjectAddGeoObjectCommand(commitId, code, type, edgeType, geoObjectType, geoObjectCode, direction);
  }

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(TypeAndCode.build(edgeType, Type.BUSINESS_EDGE));
  }

}
