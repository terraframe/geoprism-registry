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
package net.geoprism.registry.service.request;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.view.Page;

@Service
public class ClassificationService
{
  @Autowired
  private ClassificationTypeBusinessServiceIF typeService;

  @Autowired
  private ClassificationBusinessServiceIF     classificationService;

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String classificationCode, String parentCode, JsonObject object, boolean isNew)
  {
    ClassificationType type = this.typeService.getByCode(classificationCode);

    Classification parent = parentCode != null ? this.classificationService.get(type, parentCode) : null;

    Classification classification = this.classificationService.construct(type, object, isNew);

    this.classificationService.populate(classification, object);
    this.classificationService.apply(classification, parent);

    // Return the refreshed copy of the geoObject
    return classification.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String classificationCode, String code)
  {
    ClassificationType type = this.typeService.getByCode(classificationCode);

    Classification classification = this.classificationService.get(type, code);

    this.classificationService.delete(classification);
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String classificationCode, String code)
  {
    ClassificationType type = this.typeService.getByCode(classificationCode);

    Classification classification = this.classificationService.get(type, code);

    return classification.toJSON();
  }

  @Request(RequestType.SESSION)
  public void move(String sessionId, String classificationCode, String code, String parentCode)
  {
    ClassificationType type = this.typeService.getByCode(classificationCode);
    Classification classification = this.classificationService.get(type, code);
    Classification newParent = this.classificationService.get(type, parentCode);

    this.classificationService.move(classification, newParent);
  }

  @Request(RequestType.SESSION)
  public void addChild(String sessionId, String classificationCode, String parentCode, String childCode)
  {
    ClassificationType type = this.typeService.getByCode(classificationCode);

    Classification parent = this.classificationService.get(type, parentCode);
    Classification child = this.classificationService.get(type, childCode);

    this.classificationService.addChild(parent, child);
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String classificationCode, String parentCode, String childCode)
  {
    ClassificationType type = this.typeService.getByCode(classificationCode);

    Classification parent = this.classificationService.get(type, parentCode);
    Classification child = this.classificationService.get(type, childCode);

    this.classificationService.removeChild(parent, child);
  }

  @Request(RequestType.SESSION)
  public JsonObject getChildren(String sessionId, String classificationCode, String code, Integer pageSize, Integer pageNumber)
  {
    ClassificationType type = this.typeService.getByCode(classificationCode);

    if (code != null)
    {
      Classification parent = this.classificationService.get(type, code);

      return this.classificationService.getChildren(parent, pageSize, pageNumber).toJSON();
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
    ClassificationType type = this.typeService.getByCode(classificationCode);

    Classification child = this.classificationService.get(type, code);

    return this.classificationService.getAncestorTree(child, rootCode, pageSize).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonArray search(String sessionId, String classificationCode, String rootCode, String text)
  {
    ClassificationType type = this.typeService.getByCode(classificationCode);
    List<Classification> results = this.classificationService.search(type, rootCode, text);

    return results.stream().map(child -> child.toJSON()).collect(Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
      x1.addAll(x2);
      return x1;
    }));
  }

}
