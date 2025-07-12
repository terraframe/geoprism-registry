package net.geoprism.registry.axon.event.repository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.geoprism.registry.Commit;

public class InMemoryEventMerger
{
  private Map<String, LinkedList<RepositoryEvent>> groupedEvents = new HashMap<>();

  private Commit                                   previous;

  public InMemoryEventMerger()
  {
    this(null);
  }

  public InMemoryEventMerger(Commit previous)
  {
    this.previous = previous;
  }

  public void add(RepositoryEvent e)
  {
    this.groupedEvents.computeIfAbsent(e.getAggregate(), s -> new LinkedList<RepositoryEvent>());

    this.groupedEvents.get(e.getAggregate()).add(e);
  }

  public List<RepositoryEvent> buildEvents()
  {
    List<RepositoryEvent> list = new LinkedList<>();

    this.groupedEvents.forEach((key, events) -> {
      this.merge(events).ifPresent(list::add);
    });

    return list;
  }

  private <T> Optional<T> merge(LinkedList<T> events)
  {
    if (events.size() > 0)
    {
      T last = events.getLast();

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
