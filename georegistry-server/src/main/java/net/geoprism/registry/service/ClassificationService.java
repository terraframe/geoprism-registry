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
package net.geoprism.registry.service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.view.Page;

@Component
public class ClassificationService
{
  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String classificationCode, String parentCode, JsonObject object, boolean isNew)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification parent = parentCode != null ? Classification.get(type, parentCode) : null;

    Classification classification = Classification.construct(type, object, isNew);
    classification.populate(object);
    classification.apply(parent);

    // Return the refreshed copy of the geoObject
    return classification.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String classificationCode, String code)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification classification = Classification.get(type, code);
    classification.delete();
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String classificationCode, String code)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification classification = Classification.get(type, code);

    return classification.toJSON();
  }

  @Request(RequestType.SESSION)
  public void move(String sessionId, String classificationCode, String code, String parentCode)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);
    Classification classification = Classification.get(type, code);
    Classification newParent = Classification.get(type, parentCode);

    classification.move(newParent);
  }

  @Request(RequestType.SESSION)
  public void addChild(String sessionId, String classificationCode, String parentCode, String childCode)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification parent = Classification.get(type, parentCode);
    Classification child = Classification.get(type, childCode);

    parent.addChild(child);
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String classificationCode, String parentCode, String childCode)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification parent = Classification.get(type, parentCode);
    Classification child = Classification.get(type, childCode);

    parent.removeChild(child);
  }

  @Request(RequestType.SESSION)
  public JsonObject getChildren(String sessionId, String classificationCode, String code, Integer pageSize, Integer pageNumber)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    if (code != null)
    {
      Classification parent = Classification.get(type, code);

      return parent.getChildren(pageSize, pageNumber).toJSON();
    }

    Classification root = type.getRoot();
    List<Classification> roots = new LinkedList<Classification>();

    if (root != null)
    {
      roots.add(root);
    }

    return new Page<Classification>(roots.size(), pageNumber, pageSize, roots).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getAncestorTree(String sessionId, String classificationCode, String rootCode, String code, Integer pageSize)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification child = Classification.get(type, code);

    return child.getAncestorTree(rootCode, pageSize).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonArray search(String sessionId, String classificationCode, String rootCode, String text)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);
    List<Classification> results = Classification.search(type, rootCode, text);

    return results.stream().map(child -> child.toJSON()).collect(Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
      x1.addAll(x2);
      return x1;
    }));
  }

}
