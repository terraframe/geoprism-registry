/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.model;

import com.google.gson.JsonObject;

import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class GraphNode<T extends JsonSerializable> implements JsonSerializable
{
  private T                  object;

  private Page<GraphNode<T>> children;

  public GraphNode()
  {
  }

  public GraphNode(T object)
  {
    this.object = object;
  }

  public T getObject()
  {
    return object;
  }

  public void setObject(T object)
  {
    this.object = object;
  }

  public void setChildren(Page<GraphNode<T>> children)
  {
    this.children = children;
  }

  public Page<GraphNode<T>> getChildren()
  {
    return children;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof JsonSerializable)
    {
      return ( (JsonSerializable) obj ).equals(this.getObject());
    }

    return super.equals(obj);
  }

  @Override
  public int hashCode()
  {
    return this.getObject().hashCode();
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.add("object", this.object.toJSON());

    if (children != null)
    {

      object.add("children", this.children.toJSON());
    }

    return object;
  }
}
