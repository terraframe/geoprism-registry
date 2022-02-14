/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.stream.Collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.UndirectedGraphType;

public class UndirectedGraphTypeService
{
  @Request(RequestType.SESSION)
  public JsonArray getAll(String sessionId)
  {
    List<UndirectedGraphType> types = UndirectedGraphType.getAll();

    return types.stream().map(child -> child.toJSON()).collect(Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
      x1.addAll(x2);
      return x1;
    }));
  }

  @Request(RequestType.SESSION)
  public JsonObject create(String sessionId, String json)
  {
    JsonObject object = JsonParser.parseString(json).getAsJsonObject();

    UndirectedGraphType type = UndirectedGraphType.create(object);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return type.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject update(String sessionId, String json)
  {
    JsonObject object = JsonParser.parseString(json).getAsJsonObject();
    String code = object.get(UndirectedGraphType.CODE).getAsString();

    UndirectedGraphType type = UndirectedGraphType.getByCode(code);
    type.update(object);

    return type.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String code)
  {
    UndirectedGraphType type = UndirectedGraphType.getByCode(code);

    return type.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String code)
  {
    UndirectedGraphType type = UndirectedGraphType.getByCode(code);
    type.delete();
  }
}
