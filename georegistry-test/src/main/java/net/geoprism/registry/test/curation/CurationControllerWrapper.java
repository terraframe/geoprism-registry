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
package net.geoprism.registry.test.curation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.registry.controller.CurationController;
import net.geoprism.registry.controller.CurationController.VersionIdBody;
import net.geoprism.registry.test.AbstractTestClient;

@Component
public class CurationControllerWrapper extends AbstractTestClient
{
  @Autowired
  private CurationController controller;

  public JsonObject details(String historyId, Boolean onlyUnresolved, Integer pageSize, Integer pageNumber)
  {
    return JsonParser.parseString(responseToString(this.controller.details(historyId, onlyUnresolved, pageSize, pageNumber))).getAsJsonObject();
  }

  public JsonObject page(String historyId, Boolean onlyUnresolved, Integer pageSize, Integer pageNumber)
  {
    return JsonParser.parseString(responseToString(this.controller.page(historyId, onlyUnresolved, pageSize, pageNumber))).getAsJsonObject();
  }

  public JsonObject curate(String listTypeVersionId)
  {
    VersionIdBody body = new VersionIdBody();
    body.setListTypeVersionId(listTypeVersionId);

    return JsonParser.parseString(responseToString(this.controller.curate(body))).getAsJsonObject();
  }

}
