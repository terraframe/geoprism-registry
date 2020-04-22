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

import org.json.JSONArray;
import org.json.JSONObject;

import net.geoprism.registry.model.ServerGeoObjectType;

public class ParentReferenceProblem extends ParentReferenceProblemBase
{
  public static final Integer DEFAULT_SEVERITY = 10;
  
  public static final String TYPE = "ParentReferenceProblem";
  
  private static final long serialVersionUID = 1693574723;
  
  public ParentReferenceProblem()
  {
    super();
  }
  
  public ParentReferenceProblem(String typeCode, String label, String parentCode, JSONArray context)
  {
    this.setTypeCode(typeCode);
    this.setLabel(label);
    this.setContext(context.toString());
    this.setParentCode(parentCode);
  }
  
  @Override
  protected String buildKey()
  {
    if (this.getParentCode() != null && this.getParentCode().length() > 0)
    {
      return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + this.getParentCode() + "-" + this.getLabel();
    }
    else
    {
      return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + this.getLabel();
    }
  }
  
  public String getValidationProblemType()
  {
    return TYPE;
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = super.toJSON();
    
    ServerGeoObjectType type = ServerGeoObjectType.get(this.getTypeCode());
    
    object.put("label", this.getLabel());
    object.put("typeCode", this.getTypeCode());
    object.put("typeLabel", new JSONObject(type.getLabel().toJSON().toString()));
    object.put("context", new JSONArray(this.getContext()));

    if (this.getParentCode() != null && this.getParentCode().length() > 0)
    {
      object.put("parent", this.getParentCode());
    }

    return object;
  }
  
  @Override
  public void apply()
  {
    if (this.getSeverity() == null || this.getSeverity() == 0)
    {
      this.setSeverity(DEFAULT_SEVERITY);
    }
    
    super.apply();
  }
}
