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
