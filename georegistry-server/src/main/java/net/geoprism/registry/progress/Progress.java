/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.progress;

import com.google.gson.JsonObject;

public class Progress
{
  private Long   current;

  private Long   total;

  private String description;

  public Progress(Long current, Long total, String description)
  {
    super();
    this.current = current;
    this.total = total;
    this.description = description;
  }

  public JsonObject toJson()
  {
    JsonObject object = new JsonObject();
    object.addProperty("current", this.current);
    object.addProperty("total", this.total);
    object.addProperty("description", this.description);

    return object;
  }

}
