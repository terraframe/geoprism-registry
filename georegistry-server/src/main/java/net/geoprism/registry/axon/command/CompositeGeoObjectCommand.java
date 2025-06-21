package net.geoprism.registry.axon.command;

import java.util.List;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import net.geoprism.registry.axon.event.GeoObjectEvent;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class CompositeGeoObjectCommand
{
  @TargetAggregateIdentifier
  private String               uid;

  private List<GeoObjectEvent> events;

  public CompositeGeoObjectCommand()
  {
    super();
  }

  public CompositeGeoObjectCommand(String uid, List<GeoObjectEvent> events)
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
