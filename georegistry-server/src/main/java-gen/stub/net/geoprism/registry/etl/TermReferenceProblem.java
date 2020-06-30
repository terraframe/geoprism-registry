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

import com.google.gson.JsonObject;

public class TermReferenceProblem extends TermReferenceProblemBase
{
  public static final String TYPE = "TermReferenceProblem";
  
  private static final long serialVersionUID = -1226008655;
  
  public TermReferenceProblem()
  {
    super();
  }
  
  public TermReferenceProblem(String label, String parentCode, String mdAttributeId, String attributeCode, String attributeLabel)
  {
    this.setLabel(label);
    this.setMdAttributeId(mdAttributeId);
    this.setParentCode(parentCode);
    this.setAttributeCode(attributeCode);
    this.setAttributeLabel(attributeLabel);
  }
  
  public String getValidationProblemType()
  {
    return TYPE;
  }
  
  @Override
  protected String buildKey()
  {
    return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + this.getMdAttributeOid() + "-" + this.getLabel();
  }

  @Override
  public JsonObject toJson()
  {
    JsonObject object = super.toJson();
    
    object.addProperty("label", this.getLabel());
    object.addProperty("parentCode", this.getParentCode());
    object.addProperty("mdAttributeId", this.getMdAttributeOid());
    object.addProperty("attributeCode", this.getAttributeCode());
    object.addProperty("attributeLabel", this.getAttributeLabel());

    return object;
  }
  
  @Override
  public void apply()
  {
    if (this.getSeverity() == null || this.getSeverity() == 0)
    {
      this.setSeverity(1);
    }
    
    super.apply();
  }
}
