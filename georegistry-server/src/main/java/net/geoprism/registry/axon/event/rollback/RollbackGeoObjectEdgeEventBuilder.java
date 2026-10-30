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
    list.add(new RemoveGeoObjectEdgeEvent(this.original.getEdgeUid(), this.original.getTargetCode(), this.original.getTargetType(), this.original.getSourceCode(), this.original.getSourceType(), this.original.getEdgeType()));
    list.addAll(this.events);

    return list;

  }
}
