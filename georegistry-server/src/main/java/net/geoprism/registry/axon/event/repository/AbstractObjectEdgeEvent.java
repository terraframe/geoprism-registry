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

import java.util.Date;

import net.geoprism.registry.view.TypeInfo;

public abstract class AbstractObjectEdgeEvent extends AbstractRepositoryEvent implements ObjectEdgeEventIF
{
  public AbstractObjectEdgeEvent()
  {
    super();
  }

  public AbstractObjectEdgeEvent(String eventId)
  {
    super(eventId);
  }

  public abstract TypeInfo getSourceType();

  public abstract String getSourceCode();

  public abstract TypeInfo getTargetType();

  public abstract String getTargetCode();

  public abstract TypeInfo getEdgeType();

  public abstract Date getStartDate();

  public abstract Date getEndDate();

  public abstract String getEdgeUid();
}
