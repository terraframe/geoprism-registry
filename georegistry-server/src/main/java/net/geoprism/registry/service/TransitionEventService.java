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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.GeoObjectPermissionService;
import net.geoprism.registry.view.HistoricalRow;

@Component
public class TransitionEventService
{
  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, Integer pageSize, Integer pageNumber, String attrConditions)
  {
    return TransitionEvent.page(pageSize, pageNumber, attrConditions).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getDetails(String sessionId, String oid)
  {
    return TransitionEvent.get(oid).toJSON(true);
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, JsonObject json)
  {
    return TransitionEvent.apply(json);
  }

  @Request(RequestType.SESSION)
  public void delete(String sessionId, String eventId)
  {
    TransitionEvent.get(eventId).delete();
  }

  @Request(RequestType.SESSION)
  public JsonObject getHistoricalReport(String sessionId, String typeCode, Date startDate, Date endDate, Integer pageSize, Integer pageNumber)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    new GeoObjectPermissionService().enforceCanRead(type.getOrganization().getCode(), type);

    return HistoricalRow.getHistoricalReport(type, startDate, endDate, pageSize, pageNumber).toJSON();
  }

  @Request(RequestType.SESSION)
  public InputStream exportExcel(String sessionId, String typeCode, Date startDate, Date endDate) throws IOException
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    new GeoObjectPermissionService().enforceCanRead(type.getOrganization().getCode(), type);

    return HistoricalRow.exportToExcel(type, startDate, endDate);
  }

}
