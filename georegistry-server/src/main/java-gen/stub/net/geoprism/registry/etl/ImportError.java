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

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.JobHistory;

public class ImportError extends ImportErrorBase
{
  private static final long serialVersionUID = 1410792643;
  
  public static enum ErrorResolution
  {
    IGNORE,
    APPLY_GEO_OBJECT,
    UNRESOLVED
  }
  
  public ImportError()
  {
    super();
  }
  
  public static ImportErrorQuery queryResolutionStatus(String historyId, String resolutionStatus)
  {
    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    
    ieq.WHERE(ieq.getResolution().EQ(resolutionStatus));
    
    return ieq;
  }
  
  public JSONObject toJSON()
  {
    JSONObject jo = new JSONObject();
    
    JSONObject exception = new JSONObject();
    exception.put("type", new JSONObject(this.getErrorJson()).get("type"));
    exception.put("message", JobHistory.readLocalizedException(new JSONObject(this.getErrorJson()), Session.getCurrentLocale()));
    jo.put("exception", exception);
    
    if (this.getObjectJson() != null && this.getObjectJson().length() > 0)
    {
      jo.put("object", new JSONObject(this.getObjectJson()));
    }
    
    jo.put("objectType", this.getObjectType());
    
    jo.put("id", this.getOid());
    
    jo.put("resolution", this.getResolution());
    
    jo.put("rowNum", this.getRowIndex());
    
    return jo;
  }
  
  @Override
  public void apply()
  {
    if (this.getResolution().equals(""))
    {
      this.setResolution(ErrorResolution.UNRESOLVED.name());
    }
    
    super.apply();
  }
  
}
