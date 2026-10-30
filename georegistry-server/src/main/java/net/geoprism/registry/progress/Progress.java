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

import java.util.List;

import tools.jackson.databind.json.JsonMapper;

public class Progress
{
  private long   current;

  private long   total;

  private String description;

  public Progress()
  {
    this(1l, 1l, "");
  }

  public Progress(long current, long total, String description)
  {
    super();
    this.current = current;
    this.total = total;
    this.description = description;
  }

  public Progress add(long work)
  {
    this.current += work;

    return this;
  }

  public long getCurrent()
  {
    return current;
  }

  public void setCurrent(long current)
  {
    this.current = current;
  }

  public long getTotal()
  {
    return total;
  }

  public void setTotal(long total)
  {
    this.total = total;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public static String toJson(Progress dto)
  {
    JsonMapper mapper = JsonMapper.shared();
    return mapper.writeValueAsString(dto);
  }

  public static <T extends Progress> String toJson(List<T> dtos)
  {
    JsonMapper mapper = JsonMapper.shared();
    return mapper.writeValueAsString(dtos);
  }

  public static Progress parseJson(String json)
  {
    JsonMapper mapper = JsonMapper.shared();
    return mapper.readValue(json, Progress.class);
  }

  public static List<Progress> parseList(String json)
  {
    JsonMapper mapper = JsonMapper.shared();
    return mapper.readerForListOf(Progress.class).readValue(json);
  }

}
