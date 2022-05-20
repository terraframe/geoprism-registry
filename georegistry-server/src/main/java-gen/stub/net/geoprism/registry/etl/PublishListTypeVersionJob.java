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
package net.geoprism.registry.etl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.upload.ImportConfiguration;
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
