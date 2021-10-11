/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.graph.TransitionEvent;
import net.geoprism.registry.view.Page;

public class TransitionEventService
{
  @Request(RequestType.SESSION)
  public Page<TransitionEvent> page(String sessionId, Integer pageSize, Integer pageNumber)
  {
    return TransitionEvent.page(pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public JsonObject getDetails(String sessionId, String oid)
  {
    return TransitionEvent.get(oid).toJSON(true);
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String json)
  {
    return TransitionEvent.apply(JsonParser.parseString(json).getAsJsonObject());
  }
}
