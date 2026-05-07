package net.geoprism.registry.axon.event.rollback;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectEvent;
import net.geoprism.registry.axon.event.repository.RemoveBusinessObjectEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;

public class RollbackBusinessObjectEventBuilder extends RollbackEventBuilder
{
  private BusinessObjectApplyEvent             original;

  private LinkedList<BusinessObjectApplyEvent> events;

  public RollbackBusinessObjectEventBuilder(BusinessObjectApplyEvent original)
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
    if (event instanceof BusinessObjectEvent)
    {
      this.addEvent((BusinessObjectEvent) event);
    }
  }

  public void addEvent(BusinessObjectEvent event)
  {
    if (event instanceof BusinessObjectApplyEvent)
    {
      this.events.add((BusinessObjectApplyEvent) event);
    }
  }

  public List<RepositoryEvent> build()
  {
    List<RepositoryEvent> list = new LinkedList<>();

    if (this.original.getIsNew())
    {
      list.add(new RemoveBusinessObjectEvent(this.original.getCode(), this.original.getType()));
    }
    else if (this.events.size() > 0)
    {
      // Get the state of the business object
      BusinessObjectApplyEvent last = this.events.getLast();

      list.add(new BusinessObjectApplyEvent(last.getCode(), last.getType(), last.getObject(), false));
    }

    return list;

  }
}
