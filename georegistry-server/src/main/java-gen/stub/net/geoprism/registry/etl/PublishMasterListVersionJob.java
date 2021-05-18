package net.geoprism.registry.etl;

import com.google.gson.JsonObject;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class PublishMasterListVersionJob extends PublishMasterListVersionJobBase
{
  private static final long serialVersionUID = -314866464;
  
  public PublishMasterListVersionJob()
  {
    super();
  }

  @Override
  public JsonObject toJson()
  {
    return this.getMasterListVersion().toJSON(false);
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
    this.getMasterListVersion().publish();
  }
}
