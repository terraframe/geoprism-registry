package net.geoprism.registry.axon.event.repository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.geoprism.registry.Commit;

public class InMemoryEventMerger
{
  private Map<String, LinkedList<GeoObjectEvent>>      geoObjectEvents      = new HashMap<>();

  private Map<String, LinkedList<BusinessObjectEvent>> businessObjectEvents = new HashMap<>();

  private Commit                                       previous;

  public InMemoryEventMerger()
  {
    this(null);
  }

  public InMemoryEventMerger(Commit previous)
  {
    this.previous = previous;
  }

  public void add(GeoObjectEvent e)
  {
    this.geoObjectEvents.computeIfAbsent(e.getAggregate(), s -> new LinkedList<GeoObjectEvent>());

    this.geoObjectEvents.get(e.getAggregate()).add(e);
  }

  public void add(BusinessObjectEvent e)
  {
    this.businessObjectEvents.computeIfAbsent(e.getAggregate(), s -> new LinkedList<BusinessObjectEvent>());
    
    this.businessObjectEvents.get(e.getAggregate()).add(e);
  }
  
  public List<GeoObjectEvent> merge()
  {
    List<GeoObjectEvent> list = new LinkedList<>();

    this.geoObjectEvents.forEach((key, events) -> {
      this.merge(events).ifPresent(list::add);
    });

    return list;
  }

  private Optional<GeoObjectEvent> merge(LinkedList<GeoObjectEvent> events)
  {
    if (events.size() > 0)
    {
      GeoObjectEvent last = events.getLast();

      // If the last event is removing the parent, then that means there are no
      // parents for that type. As such we do not need to publish any events for
      // that object
      if (! ( last instanceof GeoObjectRemoveParentEvent ) || previous != null)
      {
        return Optional.of(last);
      }
    }

    return Optional.empty();
  }
}
