package net.geoprism.registry.etl;

import com.google.gson.JsonObject;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.registry.view.JsonSerializable;

public abstract class ListTypeJob extends ListTypeJobBase implements JsonSerializable
{
  private static final long serialVersionUID = 1050690354;

  public ListTypeJob()
  {
    super();
  }

  public abstract JsonObject toJSON();

  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }

}
