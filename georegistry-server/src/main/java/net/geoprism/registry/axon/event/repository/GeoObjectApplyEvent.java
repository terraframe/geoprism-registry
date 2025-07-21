package net.geoprism.registry.axon.event.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;

public class GeoObjectApplyEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String  code;

  private String  type;
  
  private Boolean isNew;

  private Boolean isImport;

  private String  object;


  public GeoObjectApplyEvent()
  {
  }

  public GeoObjectApplyEvent(String code, String type, Boolean isNew, Boolean isImport, String object)
  {
    super();

    this.code = code;
    this.type = type;
    this.isNew = isNew;
    this.isImport = isImport;
    this.object = object;
  }
  
  public String getCode()
  {
    return code;
  }
  
  public void setCode(String code)
  {
    this.code = code;
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
    return this.code + "#" + this.type + "_O_";
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
