package net.geoprism.registry.axon.event.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class RemoteGeoObjectRemoveExternalIdEvent implements RemoteEvent
{
  private String commitId;

  private String code;

  private String type;

  private String authority;

  public RemoteGeoObjectRemoveExternalIdEvent()
  {
  }

  public RemoteGeoObjectRemoveExternalIdEvent(String commitId, String code, String type, String authority)
  {
    super();
    this.commitId = commitId;
    this.code = code;
    this.type = type;
    this.authority = authority;
  }

  public String getCommitId()
  {
    return commitId;
  }

  public void setCommitId(String commitId)
  {
    this.commitId = commitId;
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

  public String getAuthority()
  {
    return authority;
  }

  public void setAuthority(String authority)
  {
    this.authority = authority;
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    return this.code + "#" + this.type + "_S_" + this.authority;
  }

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(TypeInfo.build(type, TypeClass.GEO_OBJECT_TYPE));
  }
}
