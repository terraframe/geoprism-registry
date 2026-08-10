package net.geoprism.registry.axon.event.remote;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class RemoteGeoObjectApplyExternalIdEvent implements RemoteEvent
{
  private String         commitId;

  private String         code;

  private String         type;

  private String         authority;

  private String         externalId;

  private ImportStrategy strategy;

  public RemoteGeoObjectApplyExternalIdEvent()
  {
  }

  public RemoteGeoObjectApplyExternalIdEvent(String commitId, String code, String type, String authority, String externalId, ImportStrategy strategy)
  {
    this.commitId = commitId;
    this.code = code;
    this.type = type;
    this.authority = authority;
    this.externalId = externalId;
    this.strategy = strategy;
  }

  @Override
  public String getCommitId()
  {
    return this.commitId;
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

  public String getExternalId()
  {
    return externalId;
  }

  public void setExternalId(String externalId)
  {
    this.externalId = externalId;
  }

  public ImportStrategy getStrategy()
  {
    return strategy;
  }

  public void setStrategy(ImportStrategy strategy)
  {
    this.strategy = strategy;
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
