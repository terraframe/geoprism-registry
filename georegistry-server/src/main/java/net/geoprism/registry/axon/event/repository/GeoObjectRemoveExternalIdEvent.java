package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class GeoObjectRemoveExternalIdEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String   code;

  private TypeInfo type;

  private String   authority;

  public GeoObjectRemoveExternalIdEvent()
  {
  }

  public GeoObjectRemoveExternalIdEvent(String code, TypeInfo type, String authority)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.authority = authority;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public TypeInfo getType()
  {
    return type;
  }

  public void setType(TypeInfo type)
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
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getTypes().stream().anyMatch(this.getType()::equals);
  }
}
