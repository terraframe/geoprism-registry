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
package net.geoprism.registry.etl.export;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.view.JsonSerializable;

public class ExportError extends ExportErrorBase implements JsonSerializable
{
  private static final long serialVersionUID = -1831328547;
  
  public ExportError()
  {
    super();
  }
  
  public JsonObject toJSON()
  {
    JsonObject jo = new JsonObject();
    
    if (this.getCode() != null && this.getCode().length() > 0)
    {
      jo.addProperty("code", this.getCode());
    }
    
    if (this.getErrorMessage() != null && this.getErrorMessage().length() > 0)
    {
      jo.addProperty("message", this.getErrorMessage());
    }
    else if (this.getErrorJson() != null && this.getErrorJson().length() > 0)
    {
      jo.addProperty("message", JobHistory.readLocalizedException(new JSONObject(this.getErrorJson()), Session.getCurrentLocale()));
    }
    
    jo.addProperty("id", this.getOid());
    
    jo.addProperty("rowNum", this.getRowIndex());
    
    return jo;
  }
  
}
