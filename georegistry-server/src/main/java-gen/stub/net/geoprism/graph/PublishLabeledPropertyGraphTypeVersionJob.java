package net.geoprism.graph;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class PublishLabeledPropertyGraphTypeVersionJob extends PublishLabeledPropertyGraphTypeVersionJobBase implements JsonSerializable
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 555765208;
  
  public PublishLabeledPropertyGraphTypeVersionJob()
  {
    super();
  }
  
  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }

  public JsonObject toJSON()
  {
    return this.getVersion().toJSON();
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

  public JsonObject toJSON(JobHistory history)
  {
    JsonObject jo = new JsonObject();

    jo.addProperty("status", history.getStatus().get(0).name());

    if (history.getStatus().get(0).equals(AllJobStatus.FAILURE) && history.getErrorJson().length() > 0)
    {
      JsonObject exception = new JsonObject();

      exception.add("type", JsonParser.parseString(history.getErrorJson()).getAsJsonObject().get("type"));
      exception.addProperty("message", history.getLocalizedError(Session.getCurrentLocale()));

      jo.add("exception", exception);
    }

    return jo;
  }


}
