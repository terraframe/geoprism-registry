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
package net.geoprism.registry.progress;

import com.google.gson.JsonObject;

public class Progress
{
  private Long   current;

  private Long   total;

  private String description;

  public Progress()
  {
    this(1l, 1l, "");
  }

  public Progress(Long current, Long total, String description)
  {
    super();
    this.current = current;
    this.total = total;
    this.description = description;
  }

  public Long getCurrent()
  {
    return current;
  }
  
  public Long getTotal()
  {
    return total;
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
