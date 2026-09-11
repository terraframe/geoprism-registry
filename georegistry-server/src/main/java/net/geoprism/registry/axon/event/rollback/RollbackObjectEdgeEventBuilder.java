package net.geoprism.registry.axon.event.rollback;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.registry.axon.event.repository.AbstractObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.ObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.ObjectEdgeEventIF;
import net.geoprism.registry.axon.event.repository.ObjectRemoveEdgeEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;

public class RollbackObjectEdgeEventBuilder extends RollbackEventBuilder
{
  private ObjectEdgeEventIF             original;

  private LinkedList<ObjectEdgeEventIF> events;

  public RollbackObjectEdgeEventBuilder(ObjectEdgeEventIF original)
  {
    this.original = original;
    this.events = new LinkedList<>();
  }

  public ObjectEdgeEventIF getOriginal()
  {
    return original;
  }

  public void addEvent(RepositoryEvent event)
  {
    if (event instanceof ObjectEdgeEventIF)
    {
      this.addEvent((ObjectEdgeEventIF) event);
    }
  }

  public void addEvent(ObjectEdgeEventIF event)
  {
    this.events.add(event);
  }

  public List<RepositoryEvent> build()
  {
    List<RepositoryEvent> list = new LinkedList<>();

    // Ensure the edge doesn't exist and then replay all of the events to derive
    // the final state before rollback
    if (! ( this.original instanceof ObjectRemoveEdgeEvent ))
    {
      list.add(createInverse(this.original));
    }

    list.addAll(this.events);

    return list;
  }

  public AbstractObjectEdgeEvent createInverse(ObjectEdgeEventIF aEvent)
  {
    if (aEvent instanceof ObjectApplyEdgeEvent)
    {
      ObjectApplyEdgeEvent event = (ObjectApplyEdgeEvent) aEvent;

      return new ObjectRemoveEdgeEvent(event.getEdgeUid(), event.getSourceCode(), event.getSourceType(), event.getEdgeType(), event.getTargetCode(), event.getTargetType(), event.getStartDate(), event.getEndDate(), event.getDataSource());
    }
    else if (aEvent instanceof ObjectRemoveEdgeEvent)
    {
      ObjectRemoveEdgeEvent event = (ObjectRemoveEdgeEvent) aEvent;

      return new ObjectApplyEdgeEvent(event.getEdgeUid(), event.getSourceCode(), event.getSourceType(), event.getEdgeType(), event.getTargetCode(), event.getTargetType(), event.getStartDate(), event.getEndDate(), event.getDataSource(), ImportStrategy.NEW_AND_UPDATE, false, null);
    }

    throw new UnsupportedOperationException();
  }
}
