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

import java.text.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.etl.ListTypeJob;
import net.geoprism.registry.service.ListTypeService;

@Controller(url = "list-type")
public class ListTypeController
{
  private ListTypeService service;

  public ListTypeController()
  {
    this.service = new ListTypeService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "list-all")
  public ResponseIF listAll(ClientRequestIF request)
  {
    return new RestBodyResponse(this.service.listAll(request.getSessionId()));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "list-for-type")
  public ResponseIF listForType(ClientRequestIF request, @RequestParamter(name = "typeCode", required = true) String typeCode)
  {
    JsonObject response = this.service.listForType(request.getSessionId(), typeCode);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "list", required = true) String listJSON)
  {
    JsonObject list = JsonParser.parseString(listJSON).getAsJsonObject();

    JsonObject response = this.service.apply(request.getSessionId(), list);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "create-entries")
  public ResponseIF createEntries(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    JsonObject response = this.service.createEntries(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "create-version")
  public ResponseIF createVersion(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "metadata", required = true) String metadata) throws ParseException
  {
    JsonObject response = this.service.createVersion(request.getSessionId(), oid, metadata);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply-version")
  public ResponseIF applyVersion(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "metadata", required = true) String metadata) throws ParseException
  {
    JsonObject response = this.service.applyVersion(request.getSessionId(), oid, metadata);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "publish")
  public ResponseIF publish(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid) throws ParseException
  {
    JsonObject response = this.service.publishVersion(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    JsonObject response = this.service.get(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "entries")
  public ResponseIF entries(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    JsonObject response = this.service.getEntries(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "versions")
  public ResponseIF versions(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    JsonArray response = this.service.getVersions(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "version")
  public ResponseIF version(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    JsonObject response = this.service.getVersion(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "fetchVersionsAsListVersion")
  public ResponseIF fetchVersionsAsListVersion(ClientRequestIF request, @RequestParamter(name = "oids", required = true) String oid)
  {
    JsonElement response = this.service.fetchVersionsAsListVersion(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove-version")
  public ResponseIF removeVersion(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    this.service.removeVersion(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "data")
  public ResponseIF data(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "criteria", required = true) String criteria, 
      @RequestParamter(name = "showInvalid", required = true) Boolean showInvalid,      
      @RequestParamter(name = "includeGeometries") Boolean includeGeometries)
  {
    JsonObject response = this.service.data(request.getSessionId(), oid, criteria, showInvalid, includeGeometries);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "record")
  public ResponseIF record(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "uid", required = true) String uid)
  {
    JsonObject response = this.service.record(request.getSessionId(), oid, uid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "values")
  public ResponseIF values(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "value") String value, 
      @RequestParamter(name = "attributeName", required = true) String attributeName,
      @RequestParamter(name = "criteria") String criteria)
  {
    JsonArray response = this.service.values(request.getSessionId(), oid, value, attributeName, criteria);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "export-shapefile", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportShapefile(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "criteria") String criteria) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(request.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();

    return new InputStreamResponse(service.exportShapefile(request.getSessionId(), oid, criteria), "application/zip", code + ".zip");
  }

  @Endpoint(url = "download-shapefile", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadShapefile(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "filter") String filter) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(request.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();

    return new InputStreamResponse(service.downloadShapefile(request.getSessionId(), oid), "application/zip", code + ".zip");
  }

  // @Endpoint(url = "generate-shapefile", method = ServletMethod.POST, error =
  // ErrorSerialization.JSON)
  // public ResponseIF generateShapefile(ClientRequestIF request,
  // @RequestParamter(name = "oid") String oid) throws JSONException
  // {
  // final String jobId = service.generateShapefile(request.getSessionId(),
  // oid);
  //
  // final RestResponse response = new RestResponse();
  // response.set("job", jobId);
  //
  // return response;
  // }

  @Endpoint(url = "export-spreadsheet", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportSpreadsheet(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "criteria") String criteria) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(request.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();

    return new InputStreamResponse(service.exportSpreadsheet(request.getSessionId(), oid, criteria), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", code + ".xlsx");
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "progress")
  public ResponseIF progress(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {

    JsonObject response = this.service.progress(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-publish-jobs")
  public ResponseIF getPublishJobs(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "pageSize", required = true) Integer pageSize, 
      @RequestParamter(name = "pageNumber", required = true) Integer pageNumber, 
      @RequestParamter(name = "sortAttr") String sortAttr, 
      @RequestParamter(name = "isAscending") Boolean isAscending)
  {
    if (sortAttr == null || sortAttr == "")
    {
      sortAttr = ListTypeJob.CREATEDATE;
    }

    if (isAscending == null)
    {
      isAscending = true;
    }

    JsonObject config = this.service.getPublishJobs(request.getSessionId(), oid, pageSize, pageNumber, sortAttr, isAscending);

    return new RestBodyResponse(config.toString());
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-publish-job")
  public ResponseIF getPublishJob(ClientRequestIF request, 
      @RequestParamter(name = "historyOid", required = true) String historyOid)
  {
    JsonObject job = this.service.getPublishJob(request.getSessionId(), historyOid);
    
    return new RestBodyResponse(job);
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF tile(ClientRequestIF request, 
      @RequestParamter(name = "x", required = true) Integer x, 
      @RequestParamter(name = "y", required = true) Integer y, 
      @RequestParamter(name = "z", required = true) Integer z, 
      @RequestParamter(name = "config", required = true) String config) throws JSONException
  {
    JSONObject object = new JSONObject(config);
    object.put("x", x);
    object.put("y", y);
    object.put("z", z);

    return new InputStreamResponse(this.service.getTile(request.getSessionId(), object), "application/x-protobuf", null);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-public-versions")
  public ResponseIF getPublicVersions(ClientRequestIF request, @RequestParamter(name = "oid", required = true) String oid)
  {
    return new RestBodyResponse(this.service.getPublicVersions(request.getSessionId(), oid));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-geospatial-versions")
  public ResponseIF getGeospatialVersions(ClientRequestIF request, @RequestParamter(name = "startDate") String startDate, @RequestParamter(name = "endDate") String endDate)
  {
    return new RestBodyResponse(this.service.getGeospatialVersions(request.getSessionId(), startDate, endDate));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "bounds")
  public ResponseIF getGeoObjectBounds(ClientRequestIF request, 
      @RequestParamter(name = "oid", required = true) String oid, 
      @RequestParamter(name = "uid") String uid)
  {
    JsonArray bounds = this.service.getBounds(request.getSessionId(), oid, uid);

    if (bounds != null)
    {
      return new RestBodyResponse(bounds);
    }

    return new RestResponse();
  }
}
