/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
    this.groupedEvents.computeIfAbsent(e.getBaseObjectId(), s -> new LinkedList<RepositoryEvent>());

    this.groupedEvents.get(e.getBaseObjectId()).add(e);
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
