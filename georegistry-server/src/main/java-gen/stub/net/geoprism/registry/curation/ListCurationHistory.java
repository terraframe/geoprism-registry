package net.geoprism.registry.curation;

import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.ListTypeVersion;

public class ListCurationHistory extends ListCurationHistoryBase
{
  private static final long serialVersionUID = -1090701307;

  public ListCurationHistory()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    deleteAllCurationProblems();

    super.delete();
  }

  public boolean hasCurationProblems()
  {
    CurationProblemQuery query = new CurationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));
    return query.getCount() > 0;
  }

  public void deleteAllCurationProblems()
  {
    CurationProblemQuery query = new CurationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    try (OIterator<? extends CurationProblem> it = query.getIterator();)
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
  }

  public List<? extends CurationProblem> getAllCurationProblems()
  {
    CurationProblemQuery query = new CurationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    try (OIterator<? extends CurationProblem> it = query.getIterator())
    {
      return it.getAll();
    }
  }

  public static void deleteAll(ListTypeVersion version)
  {
    ListCurationHistoryQuery query = new ListCurationHistoryQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    try (OIterator<? extends ListCurationHistory> it = query.getIterator();)
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
  }

}
