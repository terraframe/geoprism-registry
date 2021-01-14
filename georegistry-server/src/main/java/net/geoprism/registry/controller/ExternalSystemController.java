/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.dhis2.DHIS2FeatureService;
import net.geoprism.registry.dhis2.DHIS2PluginZipManager;
import net.geoprism.registry.service.ExternalSystemService;

@Controller(url = "external-system")
public class ExternalSystemController
{
  private ExternalSystemService service;

  public ExternalSystemController()
  {
    this.service = new ExternalSystemService();
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "system-capabilities")
  public ResponseIF getSystemCapabilities(ClientRequestIF request, @RequestParamter(name = "system") String systemJSON)
  {
    JsonObject capabilities = new DHIS2FeatureService().getSystemCapabilities(request.getSessionId(), systemJSON);
    
    return new RestBodyResponse(capabilities);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "download-dhis2-plugin")
  public ResponseIF downloadDhis2Plugin(ClientRequestIF request) throws FileNotFoundException
  {
    File pluginZip = DHIS2PluginZipManager.getPluginZip();
    
    return new InputStreamResponse(new FileInputStream(pluginZip), "application/zip", "cgr-dhis2-app.zip");
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-all")
  public ResponseIF listOrg(ClientRequestIF request, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize)
  {
    JsonObject response = this.service.page(request.getSessionId(), pageNumber, pageSize);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "system") String systemJSON)
  {
    JsonObject response = this.service.apply(request.getSessionId(), systemJSON);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    JsonObject response = this.service.get(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }
}
