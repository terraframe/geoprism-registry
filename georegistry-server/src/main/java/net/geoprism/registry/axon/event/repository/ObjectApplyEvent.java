package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.ObjectOverTimeDTO;

public abstract class ObjectApplyEvent extends AbstractRepositoryEvent
{
  private String            code;

  private String            type;

  private ObjectOverTimeDTO object;

  private Boolean           isNew;

  public ObjectApplyEvent()
  {
  }

  public ObjectApplyEvent(String code, String type, ObjectOverTimeDTO object, Boolean isNew)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.object = object;
    this.isNew = isNew;
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

  public ObjectOverTimeDTO getObject()
  {
    return object;
  }

  public void setObject(ObjectOverTimeDTO object)
  {
    this.object = object;
  }

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.OBJECT;
  }
}
