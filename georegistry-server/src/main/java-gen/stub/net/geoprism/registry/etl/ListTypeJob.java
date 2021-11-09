package net.geoprism.registry.etl;

import com.google.gson.JsonObject;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

public abstract class ListTypeJob extends ListTypeJobBase
{
  private static final long serialVersionUID = 1050690354;

  public ListTypeJob()
  {
    super();
  }

  public abstract JsonObject toJson();

  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }

}
