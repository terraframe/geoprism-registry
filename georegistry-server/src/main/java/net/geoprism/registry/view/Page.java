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
package net.geoprism.registry.view;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Page<T extends JsonSerializable>
{
  private Long    count;

  private Integer pageNumber;

  private Integer pageSize;

  private List<T> results;

  public Page()
  {
  }

  public Page(Integer count, Integer pageNumber, Integer pageSize, List<T> results)
  {
    this(count.longValue(), pageNumber, pageSize, results);
  }

  public Page(Long count, Integer pageNumber, Integer pageSize, List<T> results)
  {
    super();
    this.count = count;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.results = results;
  }

  public Long getCount()
  {
    return count;
  }

  public void setCount(Long count)
  {
    this.count = count;
  }

  public Integer getPageNumber()
  {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber)
  {
    this.pageNumber = pageNumber;
  }

  public Integer getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(Integer pageSize)
  {
    this.pageSize = pageSize;
  }

  public List<T> getResults()
  {
    return results;
  }

  public void setResults(List<T> results)
  {
    this.results = results;
  }

  public JsonObject toJSON()
  {
    JsonArray array = new JsonArray();

    for (JsonSerializable result : results)
    {
      array.add(result.toJSON());
    }

    JsonObject object = new JsonObject();
    object.addProperty("count", this.count);
    object.addProperty("pageNumber", this.pageNumber);
    object.addProperty("pageSize", this.pageSize);
    object.add("resultSet", array);

    return object;
  }

}
