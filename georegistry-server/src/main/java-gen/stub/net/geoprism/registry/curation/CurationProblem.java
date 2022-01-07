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
package net.geoprism.registry.curation;

import com.google.gson.JsonObject;

import net.geoprism.registry.view.JsonSerializable;

public class CurationProblem extends CurationProblemBase implements JsonSerializable
{
  private static final long serialVersionUID = -1322764109;

  public static enum CurationResolution {
    RESOLVED, UNRESOLVED
  }

  public CurationProblem()
  {
    super();
  }

  public JsonObject toJSON()
  {
    JsonObject json = new JsonObject();

    json.addProperty("resolution", this.getResolution());
    json.addProperty("historyId", this.getHistory().getOid());
    json.addProperty("type", this.getProblemType());
    json.addProperty("id", this.getOid());

    return json;
  }

}
