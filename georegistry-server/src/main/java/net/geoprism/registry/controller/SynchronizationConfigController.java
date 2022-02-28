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
package net.geoprism.registry.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import net.geoprism.registry.etl.fhir.FhirFactory;
import net.geoprism.registry.service.SynchronizationConfigService;

@Controller(url = "synchronization-config")
public class SynchronizationConfigController
{
  private SynchronizationConfigService service;

  public SynchronizationConfigController()
  {
    this.service = new SynchronizationConfigService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-config-for-es")
  public ResponseIF getConfigForExternalSystem(ClientRequestIF request, 
      @RequestParamter(name = "externalSystemId", required = true) String externalSystemId,
      @RequestParamter(name = "hierarchyTypeCode", required = true) String hierarchyTypeCode)
  {
    JsonObject resp = this.service.getConfigForExternalSystem(request.getSessionId(), externalSystemId, hierarchyTypeCode);

    return new RestBodyResponse(resp);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-custom-attr")
  public ResponseIF getCustomAttributeConfiguration(ClientRequestIF request, 
      @RequestParamter(name = "geoObjectTypeCode", required = true) String geoObjectTypeCode, 
      @RequestParamter(name = "externalId", required = true) String externalId)
  {
    JsonArray resp = new DHIS2FeatureService().getDHIS2AttributeConfiguration(request.getSessionId(), externalId, geoObjectTypeCode);

    return new RestBodyResponse(resp);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-all")
  public ResponseIF getAll(ClientRequestIF request, 
      @RequestParamter(name = "pageNumber", required = true) Integer pageNumber, 
      @RequestParamter(name = "pageSize", required = true) Integer pageSize)
  {
    JsonObject response = this.service.page(request.getSessionId(), pageNumber, pageSize);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "config", required = true) String configJSON)
  {
    JsonObject response = this.service.apply(request.getSessionId(), configJSON);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    JsonObject response = this.service.get(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "edit")
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    JsonElement response = this.service.edit(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "unlock")
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    this.service.unlock(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-jobs")
  public ResponseIF getJobs(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "pageNumber", required = true) Integer pageNumber,
      @RequestParamter(name = "pageSize", required = true) Integer pageSize)
  {
    JsonObject jobs = this.service.getJobs(request.getSessionId(), oid, pageSize, pageNumber);

    return new RestBodyResponse(jobs.toString());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "run")
  public ResponseIF run(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    this.service.run(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "generate-file")
  public ResponseIF generateFile(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    return new InputStreamResponse(this.service.generateFile(request.getSessionId(), oid), "application/zip", "bundles.zip");
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-fhir-export-implementations")
  public ResponseIF getFhirExportImplementations(ClientRequestIF request)
  {
    JsonArray implementations = FhirFactory.getExportImplementations();

    return new RestBodyResponse(implementations);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-fhir-import-implementations")
  public ResponseIF getFhirImportImplementations(ClientRequestIF request)
  {
    JsonArray implementations = FhirFactory.getImportImplementations();

    return new RestBodyResponse(implementations);
  }
}
