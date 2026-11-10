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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.graph.DirectedAcyclicGraphTypeSnapshotQuery;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.UndirectedGraphTypeSnapshotQuery;
import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitHasSnapshotQuery;
import net.geoprism.registry.model.SnapshotContainer;
import net.geoprism.registry.view.TypeClass;

@Service
@Primary
public class GPRGraphTypeSnapshotBusinessService extends GraphTypeSnapshotBusinessService implements GraphTypeSnapshotBusinessServiceIF
{
  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hService;

  @Override
  public GraphTypeSnapshot get(SnapshotContainer<?> version, String typeCode, String code)
  {
    if (version instanceof Commit)
    {
      if (TypeClass.DAG.getCode().equals(typeCode))
      {
        QueryFactory factory = new QueryFactory();

        CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
        vQuery.WHERE(vQuery.getParent().EQ((Commit) version));

        DirectedAcyclicGraphTypeSnapshotQuery query = new DirectedAcyclicGraphTypeSnapshotQuery(factory);
        query.WHERE(query.EQ(vQuery.getChild()));
        query.AND(query.getCode().EQ(code));

        try (OIterator<? extends GraphTypeSnapshot> it = query.getIterator())
        {
          if (it.hasNext())
          {
            return it.next();
          }
        }
      }
      else if (TypeClass.HIERARCHY.getCode().equals(typeCode))
      {
        return this.hService.get(version, code);
      }
      else if (TypeClass.UNDIRECTED_GRAPH.getCode().equals(typeCode))
      {
        QueryFactory factory = new QueryFactory();

        CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
        vQuery.WHERE(vQuery.getParent().EQ((Commit) version));

        UndirectedGraphTypeSnapshotQuery query = new UndirectedGraphTypeSnapshotQuery(factory);
        query.WHERE(query.EQ(vQuery.getChild()));
        query.AND(query.getCode().EQ(code));

        try (OIterator<? extends GraphTypeSnapshot> it = query.getIterator())
        {
          if (it.hasNext())
          {
            return it.next();
          }
        }
      }

    }

    return super.get(version, typeCode, code);
  }
}
