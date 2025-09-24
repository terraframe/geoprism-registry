package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class BusinessObjectApplyEvent extends AbstractRepositoryEvent implements BusinessObjectEvent
{
  private String  code;

  private String  type;

  private String  object;

  private Boolean isNew;

  public BusinessObjectApplyEvent()
  {
  }

  public BusinessObjectApplyEvent(String code, String type, String object, Boolean isNew)
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

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
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
  public String getBaseObjectId()
  {
    return this.code + "#" + this.type + "#B";
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
    return dto.getBusinessTypes().anyMatch(this.getType()::equals);
  }
}
