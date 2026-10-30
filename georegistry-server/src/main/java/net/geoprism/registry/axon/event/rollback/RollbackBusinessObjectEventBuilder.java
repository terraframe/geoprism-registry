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

import net.geoprism.registry.axon.event.repository.BaseObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.RemoveObjectEvent;
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
    if (event instanceof BaseObjectEvent)
    {
      this.addEvent((BaseObjectEvent) event);
    }
  }

  public void addEvent(BaseObjectEvent event)
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
      list.add(new RemoveObjectEvent(this.original.getCode(), this.original.getType()));
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
