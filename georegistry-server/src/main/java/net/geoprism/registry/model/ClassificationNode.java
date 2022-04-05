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
package net.geoprism.registry.model;

import com.google.gson.JsonObject;

import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class ClassificationNode implements JsonSerializable
{
  private Classification           classification;

  private Page<ClassificationNode> children;

  public ClassificationNode()
  {
  }

  public ClassificationNode(Classification classification)
  {
    this.classification = classification;
  }

  public void setClassification(Classification classification)
  {
    this.classification = classification;
  }

  public Classification getClassification()
  {
    return classification;
  }

  public void setChildren(Page<ClassificationNode> children)
  {
    this.children = children;
  }

  public Page<ClassificationNode> getChildren()
  {
    return children;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof ClassificationNode)
    {
      return ( (ClassificationNode) obj ).getClassification().getOid().equals(this.getClassification().getOid());
    }

    return super.equals(obj);
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.add("classification", this.classification.toJSON());

    if (children != null)
    {

      object.add("children", this.children.toJSON());
    }

    return object;
  }
}
