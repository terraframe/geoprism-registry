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
package net.geoprism.registry.test.curation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;

import net.geoprism.registry.curation.CurationController;
import net.geoprism.registry.test.TestControllerWrapper;
import net.geoprism.registry.test.TestRegistryAdapterClient;

public class CurationControllerWrapper extends TestControllerWrapper
{

  private CurationController controller = new CurationController();
  
  public CurationControllerWrapper(TestRegistryAdapterClient adapter, ClientRequestIF clientRequest)
  {
    super(adapter, clientRequest);
  }
  
  public JsonObject details(String historyId, Boolean onlyUnresolved, Integer pageSize, Integer pageNumber)
  {
    return JsonParser.parseString(responseToString(this.controller.details(this.clientRequest, historyId, onlyUnresolved, pageSize, pageNumber))).getAsJsonObject();
  }
  
  public JsonObject page(String historyId, Boolean onlyUnresolved, Integer pageSize, Integer pageNumber)
  {
    return JsonParser.parseString(responseToString(this.controller.page(this.clientRequest, historyId, onlyUnresolved, pageSize, pageNumber))).getAsJsonObject();
  }
  
  public JsonObject curate(String listTypeVersionId)
  {
    return JsonParser.parseString(responseToString(this.controller.curate(this.clientRequest, listTypeVersionId))).getAsJsonObject();
  }

}
