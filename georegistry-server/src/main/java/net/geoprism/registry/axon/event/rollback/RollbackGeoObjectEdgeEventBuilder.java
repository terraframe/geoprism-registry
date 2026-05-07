package net.geoprism.registry.axon.event.rollback;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.registry.axon.event.repository.AbstractGeoObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RemoveGeoObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;

public class RollbackGeoObjectEdgeEventBuilder extends RollbackEventBuilder
{
  private AbstractGeoObjectEdgeEvent             original;

  private LinkedList<AbstractGeoObjectEdgeEvent> events;

  public RollbackGeoObjectEdgeEventBuilder(AbstractGeoObjectEdgeEvent original)
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
    if (event instanceof AbstractGeoObjectEdgeEvent)
    {
      this.addEvent((AbstractGeoObjectEdgeEvent) event);
    }
  }

  public void addEvent(AbstractGeoObjectEdgeEvent event)
  {
    this.events.add(event);
  }

  public List<RepositoryEvent> build()
  {
    List<RepositoryEvent> list = new LinkedList<>();

    // Reset the edge and the replay all of the events
    list.add(new RemoveGeoObjectEdgeEvent(this.original.getTargetCode(), this.original.getTargetType(), this.original.getSourceCode(), this.original.getSourceType(), this.original.getEdgeTypeCode(), this.original.getEdgeClassType()));
    list.addAll(this.events);

    return list;

  }
}
