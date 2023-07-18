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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.controller.BusinessTypeController.OidBody;
import net.geoprism.registry.etl.ListTypeJob;
import net.geoprism.registry.service.ListTypeService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class ListTypeController extends RunwaySpringController
{
  public static class ListTypeBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject list;

    public JsonObject getList()
    {
      return list;
    }

    public void setList(JsonObject list)
    {
      this.list = list;
    }
  }
  
  public static class ListVersionBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject metadata;
    
    @NotEmpty
    String oid;
    
    public JsonObject getMetadata()
    {
      return metadata;
    }
    
    public void setMetadata(JsonObject metadata)
    {
      this.metadata = metadata;
    }
    
    public String getOid()
    {
      return oid;
    }
    
    public void setOid(String oid)
    {
      this.oid = oid;
    }
  }
  
  public static final String API_PATH = "list-type";

  @Autowired
  private ListTypeService service;

  @GetMapping(API_PATH + "/list-all")
  public ResponseEntity<String> listAll()
  {
    JsonArray response = this.service.listAll(this.getSessionId());
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/list-for-type")  
  public ResponseEntity<String> listForType(@NotEmpty @RequestParam String typeCode)
  {
    JsonObject response = this.service.listForType(this.getSessionId(), typeCode);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")  
  public ResponseEntity<String> apply(@Valid @RequestBody ListTypeBody body)
  {
    JsonObject response = this.service.apply(this.getSessionId(), body.list);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/create-entries")    
  public ResponseEntity<String> createEntries(@Valid @RequestBody OidBody body)
  {
    JsonObject response = this.service.createEntries(this.getSessionId(), body.getOid());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")      
  public ResponseEntity<Void> remove(@Valid @RequestBody OidBody body)
  {
    this.service.remove(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(API_PATH + "/create-version")        
  public ResponseEntity<String> createVersion(@Valid @RequestBody ListVersionBody body)
  {
    JsonObject response = this.service.createVersion(this.getSessionId(), body.oid, body.metadata.toString());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply-version")          
  public ResponseEntity<String> applyVersion(@Valid @RequestBody ListVersionBody body) throws ParseException
  {
    JsonObject response = this.service.applyVersion(this.getSessionId(), body.oid, body.metadata.toString());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/publish")            
  public ResponseEntity<String> publish(@Valid @RequestBody OidBody body) throws ParseException
  {
    JsonObject response = this.service.publishVersion(this.getSessionId(), body.getOid());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get")              
  public ResponseEntity<String> get(@NotEmpty @RequestParam String oid)
  {
    JsonObject response = this.service.get(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/entries")                
  public ResponseEntity<String> entries(@NotEmpty @RequestParam String oid)
  {
    JsonObject response = this.service.getEntries(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/versions")                  
  public ResponseEntity<String> versions(@NotEmpty @RequestParam String oid)
  {
    JsonArray response = this.service.getVersions(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/version")                    
  public ResponseEntity<String> version(@NotEmpty @RequestParam String oid)
  {
    JsonObject response = this.service.getVersion(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/fetchVersionsAsListVersion")                      
  public ResponseEntity<String> fetchVersionsAsListVersion(@NotEmpty @RequestParam String oids)
  {
    JsonElement response = this.service.fetchVersionsAsListVersion(this.getSessionId(), oids);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove-version")            
  public ResponseEntity<Void> removeVersion(@Valid @RequestBody OidBody body)
  {
    this.service.removeVersion(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(API_PATH + "/data")
  public ResponseEntity<String> data(
      @NotEmpty @RequestParam String oid,
      @RequestParam(required = false) String criteria,
      @RequestParam(required = false) Boolean showInvalid,
      @RequestParam(required = false) Boolean includeGeometries)
  {
    if (StringUtils.isEmpty(criteria))
    {
      criteria = "{}";
    }
    if (showInvalid == null)
    {
      showInvalid = Boolean.FALSE;
    }
    if (includeGeometries == null)
    {
      includeGeometries = Boolean.FALSE;
    }
    
    JsonObject response = this.service.data(this.getSessionId(), oid, criteria, showInvalid, includeGeometries);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/record")                        
  public ResponseEntity<String> record(
      @NotEmpty @RequestParam String oid, 
      @NotEmpty @RequestParam String uid)
  {
    JsonObject response = this.service.record(this.getSessionId(), oid, uid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/values")                          
  public ResponseEntity<String> values(
      @NotEmpty @RequestParam String oid, 
      @RequestParam(required = false) String value, 
      @NotEmpty @RequestParam String attributeName,
      @RequestParam(required = false) String criteria)
  {
    JsonArray response = this.service.values(this.getSessionId(), oid, value, attributeName, criteria);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/export-shapefile")                            
  public ResponseEntity<InputStreamResource> exportShapefile(
      @NotEmpty @RequestParam String oid, 
      @RequestParam(required = false) String criteria,
      @RequestParam(required = false) String actualGeometryType) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(this.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + code + ".zip");

    InputStreamResource isr = new InputStreamResource(service.exportShapefile(this.getSessionId(), oid, criteria, actualGeometryType));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/download-shapefile")                            
  public ResponseEntity<InputStreamResource> downloadShapefile(
      @NotEmpty @RequestParam String oid, 
      @RequestParam(required = false) String filter) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(this.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + code + ".zip");

    InputStreamResource isr = new InputStreamResource(service.downloadShapefile(this.getSessionId(), oid));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);    
  }

  // @Endpoint(url = "generate-shapefile", method = ServletMethod.POST, error =
  // ErrorSerialization.JSON)
  // public ResponseEntity<String> generateShapefile(,
  // @RequestParamter(name = "oid") String oid) throws JSONException
  // {
  // final String jobId = service.generateShapefile(this.getSessionId(),
  // oid);
  //
  // final RestResponse response = new RestResponse();
  // response.set("job", jobId);
  //
  // return response;
  // }

  @GetMapping(API_PATH + "/export-spreadsheet")                              
  public ResponseEntity<InputStreamResource> exportSpreadsheet(
      @NotEmpty @RequestParam String oid, 
      @RequestParam(required = false) String criteria) throws JSONException
  {
    JsonObject masterList = this.service.getVersion(this.getSessionId(), oid);
    String code = masterList.get(ListType.TYPE_CODE).getAsString() + "-" + masterList.get(ListTypeVersion.FORDATE).getAsString();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + code + ".xlsx");

    InputStreamResource isr = new InputStreamResource(service.exportSpreadsheet(this.getSessionId(), oid, criteria));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);        
  }

  @GetMapping(API_PATH + "/progress")                                
  public ResponseEntity<String> progress(@NotEmpty @RequestParam String oid)
  {
    JsonObject response = this.service.progress(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-publish-jobs")                                  
  public ResponseEntity<String> getPublishJobs(
      @NotEmpty @RequestParam String oid, 
      @RequestParam Integer pageSize, 
      @RequestParam Integer pageNumber, 
      @RequestParam(required = false) String sortAttr, 
      @RequestParam(required = false) Boolean isAscending)
  {
    if (sortAttr == null || sortAttr.equals(""))
    {
      sortAttr = ListTypeJob.CREATEDATE;
    }

    if (isAscending == null)
    {
      isAscending = true;
    }

    JsonObject config = this.service.getPublishJobs(this.getSessionId(), oid, pageSize, pageNumber, sortAttr, isAscending);

    return new ResponseEntity<String>(config.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-publish-job")                                    
  public ResponseEntity<String> getPublishJob(
      @NotEmpty @RequestParam String historyOid)
  {
    JsonObject job = this.service.getPublishJob(this.getSessionId(), historyOid);
    
    return new ResponseEntity<String>(job.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/tile")                                      
  public ResponseEntity<InputStreamResource> tile(
      @RequestParam Integer x, 
      @RequestParam Integer y, 
      @RequestParam Integer z, 
      @NotEmpty @RequestParam String config) throws JSONException
  {
    JSONObject object = new JSONObject(config);
    object.put("x", x);
    object.put("y", y);
    object.put("z", z);
    
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "application/x-protobuf");

    InputStreamResource isr = new InputStreamResource(this.service.getTile(this.getSessionId(), object));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);        
  }

  @GetMapping(API_PATH + "/get-public-versions")                                        
  public ResponseEntity<String> getPublicVersions(@NotEmpty @RequestParam String oid)
  {
    JsonArray response = this.service.getPublicVersions(this.getSessionId(), oid);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-geospatial-versions")                                          
  public ResponseEntity<String> getGeospatialVersions(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate)
  {
    JsonArray response = this.service.getGeospatialVersions(this.getSessionId(), startDate, endDate);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/bounds")                                            
  public ResponseEntity<String> getGeoObjectBounds(
      @NotEmpty @RequestParam String oid, 
      @RequestParam(required = false) String uid)
  {
    JsonArray bounds = this.service.getBounds(this.getSessionId(), oid, uid);

    if (bounds != null)
    {
      return new ResponseEntity<String>(bounds.toString(), HttpStatus.OK);
    }

    return new ResponseEntity<String>(HttpStatus.OK);
  }
}
