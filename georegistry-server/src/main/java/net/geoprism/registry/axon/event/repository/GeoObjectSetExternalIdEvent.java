package net.geoprism.registry.axon.event.repository;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;

public class GeoObjectSetExternalIdEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String         uid;

  private String         type;

  private String         systemId;

  private String         externalId;

  private ImportStrategy strategy;

  public GeoObjectSetExternalIdEvent()
  {
  }

  public GeoObjectSetExternalIdEvent(String uid, String type, String systemId, String externalId, ImportStrategy strategy)
  {
    super();

    this.uid = uid;
    this.type = type;
    this.systemId = systemId;
    this.externalId = externalId;
    this.strategy = strategy;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getSystemId()
  {
    return systemId;
  }

  public void setSystemId(String systemId)
  {
    this.systemId = systemId;
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
  public String getAggregate()
  {
    return this.uid + "_S_" + this.systemId;
  }

  @Override
  @JsonIgnore
  public EventType getEventType()
  {
    return EventType.OBJECT;
  }
  
  @Override
  public Boolean isValidFor(Date date)
  {
    return true;
  }
}
