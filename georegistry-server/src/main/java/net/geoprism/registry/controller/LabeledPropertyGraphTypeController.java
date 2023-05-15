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

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import net.geoprism.registry.controller.BusinessTypeController.OidBody;
import net.geoprism.registry.etl.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.registry.service.LabeledPropertyGraphTypeService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class LabeledPropertyGraphTypeController extends RunwaySpringController
{
  public static class LabeledPropertyGraphTypeBody
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
  
  public static final String API_PATH = "labeled-property-graph-type";

  @Autowired
  private LabeledPropertyGraphTypeService service;

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> getAll()
  {
    JsonArray response = this.service.listAll(this.getSessionId());
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")  
  public ResponseEntity<String> apply(@Valid @RequestBody LabeledPropertyGraphTypeBody body)
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
  public ResponseEntity<String> createVersion(@Valid @RequestBody OidBody body)
  {
    JsonObject response = this.service.createVersion(this.getSessionId(), body.getOid());

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
      sortAttr = PublishLabeledPropertyGraphTypeVersionJob.CREATEDATE;
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
}
