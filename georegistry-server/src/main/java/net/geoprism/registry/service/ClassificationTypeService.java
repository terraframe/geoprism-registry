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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ClassificationType;

public class ClassificationTypeService
{
  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, String criteria)
  {
    return ClassificationType.page(JsonParser.parseString(criteria).getAsJsonObject()).toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    ClassificationType.get(oid).delete();
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String classificationCode)
  {
    return ClassificationType.getByCode(classificationCode).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String json)
  {
    String retJson = GeoRegistryUtil.applyClassificationType(json);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return JsonParser.parseString(retJson).getAsJsonObject();
  }
}
