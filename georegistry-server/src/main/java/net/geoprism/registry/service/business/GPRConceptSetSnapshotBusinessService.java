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

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.graph.ConceptSetSnapshot;
import net.geoprism.graph.ConceptSetSnapshotQuery;
import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitHasSnapshotQuery;
import net.geoprism.registry.model.SnapshotContainer;

@Service
@Primary
public class GPRConceptSetSnapshotBusinessService extends ConceptSetSnapshotBusinessService implements ConceptSetSnapshotBusinessServiceIF
{
  @Override
  public ConceptSetSnapshot get(SnapshotContainer<?> version, String code)
  {
    if (version instanceof Commit)
    {
      QueryFactory factory = new QueryFactory();

      CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
      vQuery.WHERE(vQuery.getParent().EQ((Commit) version));

      ConceptSetSnapshotQuery query = new ConceptSetSnapshotQuery(factory);
      query.WHERE(query.EQ(vQuery.getChild()));
      query.AND(query.getCode().EQ(code));

      try (OIterator<? extends ConceptSetSnapshot> it = query.getIterator())
      {
        if (it.hasNext())
        {
          return it.next();
        }
      }

      return null;

    }

    return super.get(version, code);
  }
}
