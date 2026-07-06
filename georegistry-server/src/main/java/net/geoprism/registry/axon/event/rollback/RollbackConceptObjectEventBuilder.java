package net.geoprism.registry.axon.event.rollback;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.registry.axon.event.repository.ConceptObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.ConceptObjectEvent;
import net.geoprism.registry.axon.event.repository.RemoveConceptObjectEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;

public class RollbackConceptObjectEventBuilder extends RollbackEventBuilder
{
  private ConceptObjectApplyEvent             original;

  private LinkedList<ConceptObjectApplyEvent> events;

  public RollbackConceptObjectEventBuilder(ConceptObjectApplyEvent original)
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
    if (event instanceof ConceptObjectEvent)
    {
      this.addEvent((ConceptObjectEvent) event);
    }
  }

  public void addEvent(ConceptObjectEvent event)
  {
    if (event instanceof ConceptObjectApplyEvent)
    {
      this.events.add((ConceptObjectApplyEvent) event);
    }
  }

  public List<RepositoryEvent> build()
  {
    List<RepositoryEvent> list = new LinkedList<>();

    if (this.original.getIsNew())
    {
      list.add(new RemoveConceptObjectEvent(this.original.getCode(), this.original.getType()));
    }
    else if (this.events.size() > 0)
    {
      // Get the state of the business object
      ConceptObjectApplyEvent last = this.events.getLast();

      list.add(new ConceptObjectApplyEvent(last.getCode(), last.getType(), last.getObject(), false));
    }

    return list;

  }
}
