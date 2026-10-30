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

import net.geoprism.registry.axon.event.repository.ConceptObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.ConceptObjectEvent;
import net.geoprism.registry.axon.event.repository.RemoveObjectEvent;
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
      list.add(new RemoveObjectEvent(this.original.getCode(), this.original.getType()));
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
