package net.geoprism.registry.axon.event.rollback;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.registry.axon.event.repository.AbstractObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.AbstractGeoObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RemoveObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;

public class RollbackBusinessObjectEdgeEventBuilder extends RollbackEventBuilder
{
  private AbstractObjectEdgeEvent             original;

  private LinkedList<AbstractObjectEdgeEvent> events;

  public RollbackBusinessObjectEdgeEventBuilder(AbstractObjectEdgeEvent original)
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

  public void addEvent(AbstractObjectEdgeEvent event)
  {
    this.events.add(event);
  }

  public List<RepositoryEvent> build()
  {
    List<RepositoryEvent> list = new LinkedList<>();

    // Reset the edge and the replay all of the events
    list.add(new RemoveObjectEdgeEvent(this.original.getTargetCode(), this.original.getTargetType(), this.original.getSourceCode(), this.original.getSourceType(), this.original.getEdgeType(), this.original.getStartDate(), this.original.getEndDate()));
    list.addAll(this.events);

    return list;

  }
}
