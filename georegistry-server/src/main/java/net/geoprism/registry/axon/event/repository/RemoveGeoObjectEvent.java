package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class RemoveGeoObjectEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String code;

  private String type;

  public RemoveGeoObjectEvent()
  {
  }

  public RemoveGeoObjectEvent(String code, String type)
  {
    super(UUID.randomUUID().toString());
    this.code = code;
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

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.OBJECT;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getGeoObjectTypes().anyMatch(this.getType()::equals);
  }

}
