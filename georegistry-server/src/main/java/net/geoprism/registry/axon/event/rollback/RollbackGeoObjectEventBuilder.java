package net.geoprism.registry.axon.event.rollback;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectEvent;
import net.geoprism.registry.axon.event.repository.RemoveGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;

public class RollbackGeoObjectEventBuilder extends RollbackEventBuilder
{
  private GeoObjectApplyEvent             original;

  private LinkedList<GeoObjectApplyEvent> events;

  public RollbackGeoObjectEventBuilder(GeoObjectApplyEvent original)
  {
    this.original = original;
    this.events = new LinkedList<>();
  }

  public RepositoryEvent getOriginal()
  {
    return original;
  }

  public void addEvent(RepositoryEvent event)
  {
    if (event instanceof GeoObjectEvent)
    {
      this.addEvent((GeoObjectEvent) event);
    }
  }

  public void addEvent(GeoObjectEvent event)
  {
    if (event instanceof GeoObjectApplyEvent)
    {
      this.events.add((GeoObjectApplyEvent) event);
    }
  }

  public List<RepositoryEvent> build()
  {
    List<RepositoryEvent> list = new LinkedList<>();

    if (this.original.getIsNew())
    {
      list.add(new RemoveGeoObjectEvent(this.original.getCode(), this.original.getType()));
    }
    else if (this.events.size() > 0)
    {
      // Get the state of the business object
      GeoObjectApplyEvent last = this.events.getLast();

      list.add(new GeoObjectApplyEvent(last.getCode(), last.getType(), false, last.getIsImport(), last.getObject()));
    }

    return list;

  }
}
