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
package net.geoprism.registry.service.business;

import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.Commit;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.SynchronizationHasProcessedCommit;
import net.geoprism.registry.SynchronizationHasProcessedCommitQuery;

@Service
public class SynchronizationHasProcessedCommitBusinessService implements SynchronizationHasProcessedCommitBusinessServiceIF
{

  @Override
  public boolean hasBeenPublished(SynchronizationConfig Synchronization, Commit commit)
  {
    SynchronizationHasProcessedCommitQuery query = new SynchronizationHasProcessedCommitQuery(new QueryFactory());
    query.WHERE(query.getParent().EQ(Synchronization));
    query.AND(query.getChild().EQ(commit));

    try (OIterator<? extends SynchronizationHasProcessedCommit> iterator = query.getIterator())
    {
      return iterator.hasNext();
    }
  }

  @Override
  public SynchronizationHasProcessedCommit create(SynchronizationConfig Synchronization, Commit commit)
  {
    SynchronizationHasProcessedCommit relationship = new SynchronizationHasProcessedCommit(Synchronization, commit);
    relationship.apply();

    return relationship;
  }
}
