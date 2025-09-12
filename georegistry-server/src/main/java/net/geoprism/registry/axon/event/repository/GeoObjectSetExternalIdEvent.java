package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.view.PublishDTO;

public class GeoObjectSetExternalIdEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String         code;

  private String         type;

  private String         systemId;

  private String         externalId;

  private ImportStrategy strategy;

  public GeoObjectSetExternalIdEvent()
  {
  }

  public GeoObjectSetExternalIdEvent(String code, String type, String systemId, String externalId, ImportStrategy strategy)
  {
    super(UUID.randomUUID().toString());

    this.code = code;
    this.type = type;
    this.systemId = systemId;
    this.externalId = externalId;
    this.strategy = strategy;
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
  public String getBaseObjectId()
  {
    return this.code + "#" + this.type + "_S_" + this.systemId;
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
    return dto.getGeoObjectTypes().anyMatch(this.getType()::equals);
  }
}
