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
package net.geoprism.registry.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
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

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.etl.PublishListTypeJob;
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
    JsonObject response = new JsonObject();
    response.add("lists", this.service.listAll(request.getSessionId()));

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "list-org")
  public ResponseIF listOrg(ClientRequestIF request, @RequestParamter(name = "orgCode") String orgCode)
  {
    JsonObject response = new JsonObject();
    response.add("orgs", this.service.listByOrg(request.getSessionId(), orgCode));

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "create")
  public ResponseIF create(ClientRequestIF request, @RequestParamter(name = "list") String listJSON)
  {
    JsonObject list = JsonParser.parseString(listJSON).getAsJsonObject();

    JsonObject response = this.service.create(request.getSessionId(), list);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "create-version")
  public ResponseIF createVersion(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "forDate") String forDate) throws ParseException
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    JsonObject response = this.service.createExploratoryVersion(request.getSessionId(), oid, format.parse(forDate));

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "publish")
  public ResponseIF publish(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws ParseException
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    JsonObject response = this.service.publishVersion(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "publish-versions")
  public ResponseIF publishVersions(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws ParseException
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    final String jobId = this.service.createPublishedVersionsJob(request.getSessionId(), oid);

    final RestResponse response = new RestResponse();
    response.set("job", jobId);

    return response;
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    JsonObject response = this.service.get(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "versions")
  public ResponseIF versions(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    JsonArray response = this.service.getVersions(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "version")
  public ResponseIF version(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    JsonObject response = this.service.getVersion(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove-version")
  public ResponseIF removeVersion(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    this.service.removeVersion(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "data")
  public ResponseIF data(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "includeGeometries") Boolean includeGeometries, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "filter") String filter, @RequestParamter(name = "sort") String sort)
  {
    JsonObject response = this.service.data(request.getSessionId(), oid, includeGeometries, pageNumber, pageSize, filter, sort);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "values")
  public ResponseIF values(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "value") String value, @RequestParamter(name = "attributeName") String attributeName, @RequestParamter(name = "valueAttribute") String valueAttribute, @RequestParamter(name = "filter") String filter)
  {
    JsonArray response = this.service.values(request.getSessionId(), oid, value, attributeName, valueAttribute, filter);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "export-shapefile", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportShapefile(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "filter") String filter) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(request.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();

    return new InputStreamResponse(service.exportShapefile(request.getSessionId(), oid, filter), "application/zip", code + ".zip");
  }

  @Endpoint(url = "download-shapefile", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF downloadShapefile(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "filter") String filter) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(request.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();

    return new InputStreamResponse(service.downloadShapefile(request.getSessionId(), oid), "application/zip", code + ".zip");
  }

//  @Endpoint(url = "generate-shapefile", method = ServletMethod.POST, error = ErrorSerialization.JSON)
//  public ResponseIF generateShapefile(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
//  {
//    final String jobId = service.generateShapefile(request.getSessionId(), oid);
//
//    final RestResponse response = new RestResponse();
//    response.set("job", jobId);
//
//    return response;
//  }

  @Endpoint(url = "export-spreadsheet", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportSpreadsheet(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "filter") String filter) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(request.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();

    return new InputStreamResponse(service.exportSpreadsheet(request.getSessionId(), oid, filter), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", code + ".xlsx");
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "progress")
  public ResponseIF progress(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {

    JsonObject response = this.service.progress(request.getSessionId(), oid);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-publish-jobs")
  public ResponseIF getPublishJobs(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "sortAttr") String sortAttr, @RequestParamter(name = "isAscending") Boolean isAscending)
  {
    if (sortAttr == null || sortAttr == "")
    {
      sortAttr = PublishListTypeJob.CREATEDATE;
    }

    if (isAscending == null)
    {
      isAscending = true;
    }

    JsonObject config = this.service.getPublishJobs(request.getSessionId(), oid, pageSize, pageNumber, sortAttr, isAscending);

    return new RestBodyResponse(config.toString());
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF tile(ClientRequestIF request, @RequestParamter(name = "x") Integer x, @RequestParamter(name = "y") Integer y, @RequestParamter(name = "z") Integer z, @RequestParamter(name = "config") String config) throws JSONException
  {
    JSONObject object = new JSONObject(config);
    object.put("x", x);
    object.put("y", y);
    object.put("z", z);

    return new InputStreamResponse(this.service.getTile(request.getSessionId(), object), "application/x-protobuf", null);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "list-all-versions")
  public ResponseIF listAllVersions(ClientRequestIF request)
  {
    return new RestBodyResponse(this.service.getAllVersions(request.getSessionId()));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "bounds")
  public ResponseIF getGeoObjectBounds(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    String bounds = this.service.getBounds(request.getSessionId(), oid);

    return new RestBodyResponse(bounds);
  }
}
