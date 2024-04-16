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

import java.util.List;
import java.util.stream.Collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.service.business.DirectedAcyclicGraphTypeBusinessServiceIF;

@Service
public class DirectedAcyclicGraphTypeService
{
  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF service;

  @Request(RequestType.SESSION)
  public JsonArray getAll(String sessionId)
  {
    List<DirectedAcyclicGraphType> types = DirectedAcyclicGraphType.getAll();

    return types.stream().map(child -> child.toJSON()).collect(Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
      x1.addAll(x2);
      return x1;
    }));
  }

  @Request(RequestType.SESSION)
  public JsonObject create(String sessionId, JsonObject object)
  {
    DirectedAcyclicGraphType type = this.service.create(object);

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return type.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject update(String sessionId, JsonObject object)
  {
    String code = object.get(DirectedAcyclicGraphType.CODE).getAsString();

    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.getByCode(code);

    this.service.update(type, object);

    return type.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String code)
  {
    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.getByCode(code);

    return type.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String code)
  {
    DirectedAcyclicGraphType type = DirectedAcyclicGraphType.getByCode(code);
    type.delete();
  }
}
