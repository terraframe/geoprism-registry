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
package net.geoprism.registry;

import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.jobs.GPRJobHistory;

public class RollbackCheckpoint extends RollbackCheckpointBase
{
  public static enum Status {
    SCHEDULED, RUNNING, AVAILABLE
  }

  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1291887527;

  public RollbackCheckpoint()
  {
    super();
  }

  @SuppressWarnings("unchecked")
  public static List<RollbackCheckpoint> getAll(GPRJobHistory history)
  {
    RollbackCheckpointQuery query = new RollbackCheckpointQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(history));

    try (OIterator<? extends RollbackCheckpoint> it = query.getIterator())
    {
      return (List<RollbackCheckpoint>) it.getAll();
    }
  }

}
