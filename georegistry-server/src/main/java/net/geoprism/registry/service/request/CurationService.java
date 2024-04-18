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

import net.geoprism.registry.service.business.CurationBusinessService;

@Service
public class CurationService
{
  @Autowired
  private CurationBusinessService service;

  @Request(RequestType.SESSION)
  public JsonObject details(String sessionId, String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    return this.service.details(historyId, onlyUnresolved, pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    return this.service.page(historyId, onlyUnresolved, pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public JsonObject curate(String sessionId, String listTypeVersionId)
  {
    return this.service.curate(listTypeVersionId);
  }

  @Request(RequestType.SESSION)
  public void submitProblemResolution(String sessionId, String json)
  {
    this.service.submitProblemResolution(json);
  }

  @Request(RequestType.SESSION)
  public void setResolution(String sessionId, String problemId, String resolution)
  {
    this.service.setResolution(problemId, resolution);
  }
}
