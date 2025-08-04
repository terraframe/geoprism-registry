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
      if (GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE.equals(typeCode))
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
      else if (GraphTypeSnapshot.HIERARCHY_TYPE.equals(typeCode))
      {
        return this.hService.get(version, code);
      }
      else if (GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE.equals(typeCode))
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
