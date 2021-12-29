package net.geoprism.registry.etl;

import com.google.gson.JsonObject;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class PublishListTypeVersionJob extends PublishListTypeVersionJobBase
{
  private static final long serialVersionUID = 1276323398;
  
  public PublishListTypeVersionJob()
  {
    super();
  }
  

  @Override
  public JsonObject toJSON()
  {
    return this.getVersion().toJSON(false);
  }
  
  @Override
  public void afterJobExecute(JobHistory history)
  {
    super.afterJobExecute(history);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    if (this.getRunAsUser() == null)
    {
      this.getVersion().publishNoAuth();
    }
    else
    {
      this.getVersion().publish();
    }
  }
  
}
