/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.JobHistory;

public class RowValidationProblem extends RowValidationProblemBase
{
  public static final String TYPE = "RowValidationProblem";
  
  private static final long serialVersionUID = -1193007210;
  
  public RowValidationProblem()
  {
    super();
  }
  
  public RowValidationProblem(Throwable exception)
  {
    this.setExceptionJson(JobHistory.exceptionToJson(exception).toString());
  }

  @Override
  public String buildKey()
  {
    JSONObject err = new JSONObject(this.getExceptionJson());
    
    return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + err.getString("message");
  }
  
  @Override
  public String getValidationProblemType()
  {
    return TYPE;
  }

  @Override
  public JsonObject toJson()
  {
    JsonObject object = super.toJson();
    
    JsonObject errJson = JsonParser.parseString(this.getExceptionJson()).getAsJsonObject();
    
    JsonObject exception = new JsonObject();
    exception.addProperty("type", errJson.get("type").getAsString());
    exception.addProperty("message", JobHistory.readLocalizedException(new JSONObject(this.getExceptionJson()), Session.getCurrentLocale()));
    
    object.add("exception", exception);

    return object;
  }
  
  @Override
  public void apply()
  {
    if (this.getSeverity() == null || this.getSeverity() == 0)
    {
      this.setSeverity(100);
    }
    
    super.apply();
  }
}
