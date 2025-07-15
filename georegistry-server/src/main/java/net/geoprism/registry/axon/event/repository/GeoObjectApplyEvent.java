package net.geoprism.registry.axon.event.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class GeoObjectApplyEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String  uid;

  private Boolean isNew;

  private Boolean isImport;

  private String  object;

  private String  type;

  public GeoObjectApplyEvent()
  {
  }

  public GeoObjectApplyEvent(String uid, String type, Boolean isNew, Boolean isImport, String object)
  {
    super();

    this.uid = uid;
    this.isNew = isNew;
    this.isImport = isImport;
    this.object = object;
    this.type = type;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  public Boolean getIsImport()
  {
    return isImport;
  }

  public void setIsImport(Boolean isImport)
  {
    this.isImport = isImport;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
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
  public String getAggregate()
  {
    return this.uid + "_O_";
  }

  @Override
  @JsonIgnore
  public EventType getEventType()
  {
    return EventType.OBJECT;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getGeoObjectTypes().contains(this.getType());
  }
}
