package net.geoprism.registry.axon.command.repository;

import java.util.List;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import net.geoprism.registry.axon.event.repository.GeoObjectEvent;

public class GeoObjectCompositeCommand
{
  @TargetAggregateIdentifier
  private String               key;

  private String               code;

  private String               type;

  private List<GeoObjectEvent> events;

  public GeoObjectCompositeCommand()
  {
    super();
  }

  public GeoObjectCompositeCommand(String code, String type, List<GeoObjectEvent> events)
  {
    this.key = code + "#" + type;
    this.code = code;
    this.type = type;
    this.events = events;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
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

  public List<GeoObjectEvent> getEvents()
  {
    return events;
  }

  public void setEvents(List<GeoObjectEvent> events)
  {
    this.events = events;
  }
}
