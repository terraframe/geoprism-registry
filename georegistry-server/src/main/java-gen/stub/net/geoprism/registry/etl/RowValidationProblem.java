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

import com.runwaysdk.business.SmartException;
import com.runwaysdk.system.scheduler.ExecutableJob;

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
    this.setExceptionJson(RowValidationProblem.serializeException(exception).toString());
  }
  
  public static JSONObject serializeException(Throwable exception)
  {
    JSONObject joException = new JSONObject();
    
    if (exception instanceof SmartException)
    {
      joException.put("type", ((SmartException)exception).getType());
    }
    else
    {
      joException.put("type", exception.getClass().getName());
    }
    
    String message = ExecutableJob.getMessageFromException(exception);
    joException.put("message", message);
    
    return joException;
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
  public JSONObject toJSON()
  {
    JSONObject object = super.toJSON();
    
    object.put("exception", new JSONObject(this.getExceptionJson()));

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
