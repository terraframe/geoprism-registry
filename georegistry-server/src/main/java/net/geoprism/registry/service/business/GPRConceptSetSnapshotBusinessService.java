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
