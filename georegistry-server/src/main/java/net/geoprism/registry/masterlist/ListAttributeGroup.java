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
package net.geoprism.registry.masterlist;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ListAttributeGroup implements ListColumn
{
  private String           label;

  private String           name;

  private List<ListColumn> columns;

  public ListAttributeGroup()
  {
    this("", null);
  }

  public ListAttributeGroup(String label)
  {
    this(label, null);
  }

  public ListAttributeGroup(String label, String name)
  {
    this.label = label;
    this.name = name;
    this.columns = new LinkedList<ListColumn>();
  }

  public void add(ListColumn column)
  {
    if (column != null)
    {
      this.columns.add(column);
    }
  }

  public List<ListColumn> getColumns()
  {
    return columns;
  }

  @Override
  public JsonObject toJSON()
  {
    JsonArray array = this.columns.stream().map(m -> m.toJSON()).collect(() -> new JsonArray(), (a, e) -> a.add(e), (listA, listB) -> {
    });

    JsonObject object = new JsonObject();
    object.addProperty("label", label);
    object.addProperty("colspan", this.getNumberOfColumns());
    object.addProperty("rowspan", this.getRowspan());
    object.add("columns", array);

    if (this.name != null)
    {
      object.addProperty("name", this.name);
    }

    return object;
  }

  @Override
  public int getNumberOfColumns()
  {
    return this.columns.stream().map(m -> m.getNumberOfColumns()).reduce((a, b) -> a + b).orElseGet(() -> 0);
  }

  @Override
  public Set<String> getColumnsIds()
  {
    Set<String> set = new TreeSet<String>();

    this.columns.forEach(column -> set.addAll(column.getColumnsIds()));

    return set;
  }

  @Override
  public int getRowspan()
  {
    return 1;
  }

  public String getLabel()
  {
    return label;
  }

  public String getName()
  {
    return name;
  }

  @Override
  public void visit(ListColumnVisitor visitor)
  {
    visitor.accept(this);

    this.columns.forEach(column -> column.visit(visitor));

    visitor.close(this);
  }
}
