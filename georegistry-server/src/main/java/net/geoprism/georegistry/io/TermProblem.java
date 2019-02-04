/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Runway SDK(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.georegistry.io;

import com.google.gson.JsonObject;

public class TermProblem implements Comparable<TermProblem>
{
  public static final String TYPE = "categories";

  private String             label;

  private String             mdAttributeId;

  private String             code;

  private String             attributeLabel;

  public TermProblem(String label, String mdAttributeId, String code, String attributeLabel)
  {
    this.label = label;
    this.mdAttributeId = mdAttributeId;
    this.code = code;
    this.attributeLabel = attributeLabel;
  }

  public String getMdAttributeId()
  {
    return mdAttributeId;
  }

  public String getKey()
  {
    return this.mdAttributeId + "-" + this.label;
  }

  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty("label", label);
    object.addProperty("mdAttributeId", mdAttributeId);
    object.addProperty("attributeLabel", attributeLabel);
    object.addProperty("code", code);

    return object;
  }

  @Override
  public int compareTo(TermProblem problem)
  {
    return this.getKey().compareTo(problem.getKey());
  }
}
