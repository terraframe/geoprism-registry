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

public class RowValidationProblem extends ValidationProblem
{
  private Throwable exception;
  
  private Long rowNum;

  public RowValidationProblem(Throwable exception, Long rowNum)
  {
    this.exception = exception;
    this.rowNum = rowNum;
  }

  @Override
  public String getKey()
  {
    return this.rowNum.toString();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    object.put("type", "RowValidationProblem");
    object.put("rowNum", this.rowNum);
    
    
    JSONObject joException = new JSONObject();
    
    if (exception instanceof SmartException)
    {
      joException.put("type", ((SmartException)this.exception).getType());
    }
    else
    {
      joException.put("type", this.exception.getClass().getName());
    }
    
    String message = ExecutableJob.getMessageFromException(this.exception);
    joException.put("message", message);
    
    object.put("exception", joException);

    return object;
  }
}
