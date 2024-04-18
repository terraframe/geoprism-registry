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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.business.GeoObjectEditorBusinessService;

@Service
public class GeoObjectEditorService implements GeoObjectEditorServiceIF
{
  @Autowired
  private GeoObjectEditorBusinessService service;

  @Override
  @Request(RequestType.SESSION)
  public JsonObject createGeoObject(String sessionId, String ptn, String sTimeGo, String masterListId, String notes)
  {
    return this.service.createGeoObject(ptn, sTimeGo, masterListId, notes);
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject updateGeoObject(String sessionId, String geoObjectCode, String geoObjectTypeCode, String actions, String masterListId, String notes)
  {
    return this.service.updateGeoObject(geoObjectCode, geoObjectTypeCode, actions, masterListId, notes);
  }

}
