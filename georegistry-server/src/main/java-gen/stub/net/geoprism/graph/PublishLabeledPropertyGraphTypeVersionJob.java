/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.graph;

import java.util.Date;

import com.google.gson.JsonObject;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
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
    LabeledPropertyGraphTypeVersionBusinessServiceIF service = LabeledPropertyGraphTypeVersionBusinessServiceIF.getInstance();

    return service.toJSON(getVersion());
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
    var history = (ImportHistory) executionContext.getJobHistoryRecord().getChild();
    var monitor = new LPGPublishImportHistoryProgressMonitor(history);
    
    history.appLock();
    JsonObject jo = new JsonObject();
    jo.addProperty(ImportConfiguration.FILE_NAME, "Publish LPG " + this.getVersion().getGraphType().getDisplayLabel().getValue() + " (Version " + this.getVersion().getVersionNumber() + ")");
    jo.addProperty(ImportConfiguration.OBJECT_TYPE, "LPG");
    history.setConfigJson(jo.toString());
    history.apply();
    
    LabeledPropertyGraphTypeVersionBusinessServiceIF service = LabeledPropertyGraphTypeVersionBusinessServiceIF.getInstance();

    if (this.getRunAsUser() == null)
    {
      service.publishNoAuth(monitor, getVersion());
    }
    else
    {
      service.publish(monitor, getVersion());
    }
  }
  
  /*
   * It was decided to use the ImportHistory here primarily because of the complicated 'getActiveImports' query in ETLBusinessService, which shows
   * jobs on the 'ScheduledJobs' page. Since this query brings in all sorts of complex "Organization" related criteria, it was decided
   * to use ImportHistory in order to not rock the boat, even though semantically this is not an import.
   */
  @Override
  public JobHistory createNewHistory()
  {
    ImportHistory history = new ImportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ImportStage.NEW);
    history.setWorkProgress(0L);
    history.setCompletedRowsJson("");
    history.setImportedRecords(0L);
    history.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));

    return history;
  }

//  public JsonObject toJSON(JobHistory history)
//  {
//    JsonObject jo = new JsonObject();
//
//    jo.addProperty("status", history.getStatus().get(0).name());
//
//    if (history.getStatus().get(0).equals(AllJobStatus.FAILURE) && history.getErrorJson().length() > 0)
//    {
//      JsonObject exception = new JsonObject();
//
//      exception.add("type", JsonParser.parseString(history.getErrorJson()).getAsJsonObject().get("type"));
//      exception.addProperty("message", history.getLocalizedError(Session.getCurrentLocale()));
//
//      jo.add("exception", exception);
//    }
//
//    return jo;
//  }

}
