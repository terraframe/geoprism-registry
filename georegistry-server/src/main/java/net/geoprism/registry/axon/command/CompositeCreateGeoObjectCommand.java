package net.geoprism.registry.axon.command;

import java.util.List;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import net.geoprism.registry.axon.event.GeoObjectEvent;

public class CompositeCreateGeoObjectCommand
{
  @TargetAggregateIdentifier
  private String               uid;

  private List<GeoObjectEvent> events;

  public CompositeCreateGeoObjectCommand()
  {
    super();
  }

  public CompositeCreateGeoObjectCommand(String uid, List<GeoObjectEvent> events)
  {
    this.uid = uid;
    this.events = events;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
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
