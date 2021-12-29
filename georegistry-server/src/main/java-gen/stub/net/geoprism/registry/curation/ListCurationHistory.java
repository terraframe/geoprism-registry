package net.geoprism.registry.curation;

import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class ListCurationHistory extends ListCurationHistoryBase
{
  private static final long serialVersionUID = -1090701307;
  
  public ListCurationHistory()
  {
    super();
  }
  
  @Override
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

    OIterator<? extends CurationProblem> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }
  
  public List<? extends CurationProblem> getAllCurationProblems()
  {
    CurationProblemQuery query = new CurationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    OIterator<? extends CurationProblem> it = query.getIterator();

    return it.getAll();
  }
  
}
