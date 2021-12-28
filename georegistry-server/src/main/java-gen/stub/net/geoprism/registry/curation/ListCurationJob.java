package net.geoprism.registry.curation;

import java.util.List;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.registry.ListTypeVersion;

public class ListCurationJob extends ListCurationJobBase
{
  private static final long serialVersionUID = -1676131772;
  
  public ListCurationJob()
  {
    super();
  }
  
  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    
  }
  
  @Override
  public boolean canResume(JobHistoryRecord jhr)
  {
    return true;
  }

  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }
  
  public static ListCurationHistory getMostRecent(String versionId)
  {
    ListCurationHistoryQuery query = new ListCurationHistoryQuery(new QueryFactory());
    
    query.WHERE(query.getVersion().EQ(versionId));
    
    query.ORDER_BY_DESC(query.getCreateDate());
    
    List<? extends ListCurationHistory> histories = query.getIterator().getAll();
    
    if (histories.size() > 0)
    {
      return histories.get(0);
    }
    else
    {
      return null;
    }
  }
  
  public static ListCurationHistory getRunning(String versionId)
  {
    ListCurationHistoryQuery query = new ListCurationHistoryQuery(new QueryFactory());
    
    query.WHERE(query.getVersion().EQ(versionId));
    
    query.AND(query.getStatus().containsExactly(AllJobStatus.RUNNING));
    
    List<? extends ListCurationHistory> histories = query.getIterator().getAll();
    
    if (histories.size() > 0)
    {
      return histories.get(0);
    }
    else
    {
      return null;
    }
  }
  
}
