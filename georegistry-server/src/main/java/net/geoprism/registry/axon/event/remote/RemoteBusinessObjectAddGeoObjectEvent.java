package net.geoprism.registry.axon.event.remote;

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

  private String        edgeUid;

  private String        geoObjectType;

  private String        geoObjectCode;

  private EdgeDirection direction;

  private String        dataSource;

  public RemoteBusinessObjectAddGeoObjectEvent()
  {
  }

  public RemoteBusinessObjectAddGeoObjectEvent(String commitId, String code, String type, String edgeUid, String edgeType, String geoObjectType, String geoObjectCode, EdgeDirection direction, String dataSource)
  {
    this(commitId, code + "#" + type, code, type, edgeUid, edgeType, geoObjectType, geoObjectCode, direction, dataSource);
  }

  public RemoteBusinessObjectAddGeoObjectEvent(String commitId, String key, String code, String type, String edgeUid, String edgeType, String geoObjectType, String geoObjectCode, EdgeDirection direction, String dataSource)
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
    this.dataSource = dataSource;
    this.edgeUid = edgeUid;
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

  public String getDataSource()
  {
    return dataSource;
  }

  public void setDataSource(String dataSource)
  {
    this.dataSource = dataSource;
  }

  public String getEdgeUid()
  {
    return edgeUid;
  }

  public void setEdgeUid(String edgeUid)
  {
    this.edgeUid = edgeUid;
  }

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(TypeAndCode.build(edgeType, Type.BUSINESS_EDGE));
  }

}
