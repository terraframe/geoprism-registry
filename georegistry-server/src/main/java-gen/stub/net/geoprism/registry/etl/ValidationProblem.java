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

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

public abstract class ValidationProblem extends ValidationProblemBase implements Comparable<ValidationProblem>
{
  private static final long serialVersionUID = 681333878;
  
  public static enum ValidationResolution
  {
    IGNORE,
    SYNONYM,
    CREATE,
    UNRESOLVED
  }
  
  public ValidationProblem()
  {
    super();
  }
  
  abstract public String getValidationProblemType();
  
  @Override
  public String getKey()
  {
    String key = super.getKey();
    
    if (key == null || key.length() == 0)
    {
      return this.buildKey();
    }
    else
    {
      return key;
    }
  }
  
  public JsonObject toJson()
  {
    JsonObject json = new JsonObject();
    
    json.addProperty("affectedRows", this.getAffectedRows());
    json.addProperty("resolution", this.getResolution());
    json.addProperty("historyId", this.getHistory().getOid());
    json.addProperty("type", this.getValidationProblemType());
    json.addProperty("id", this.getOid());
    
    return json;
  }
  
  public void addAffectedRowNumber(long rowNum)
  {
    String sRows = this.getAffectedRows();
    
    if (sRows.length() > 0)
    {
      ArrayList<String> lRows = new ArrayList<String>();
      
      for (String row : StringUtils.split(sRows, ","))
      {
        lRows.add(row);
      }
      
      lRows.add(String.valueOf(rowNum));
      
      sRows = StringUtils.join(lRows, ",");
    }
    else
    {
      sRows = String.valueOf(rowNum);
    }
    
    this.setAffectedRows(sRows);
  }
  
  @Override
  public int compareTo(ValidationProblem problem)
  {
    return this.getKey().compareTo(problem.getKey());
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof ValidationProblem))
    {
      return false;
    }
    
    ValidationProblem vp = (ValidationProblem) obj;
    
    return vp.getKey().equals(this.getKey());
  }
}
