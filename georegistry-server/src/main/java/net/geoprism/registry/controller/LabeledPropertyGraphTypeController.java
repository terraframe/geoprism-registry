/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.controller;

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
import com.google.gson.JsonObject;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.LabeledPropertyGraphTypeServiceIF;
import net.geoprism.spring.core.JsonObjectDeserializer;

@RestController
@Validated
public class LabeledPropertyGraphTypeController extends RunwaySpringController
{
  public static class OidBody
  {
    @NotEmpty
    private String oid;

    public String getOid()
    {
      return oid;
    }

    public void setOid(String oid)
    {
      this.oid = oid;
    }
  }

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
    String     oid;

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

  public static final String                API_PATH = RegistryConstants.CONTROLLER_ROOT + "labeled-property-graph-type";

  @Autowired
  private LabeledPropertyGraphTypeServiceIF service;

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> getAll()
  {
    JsonArray response = this.service.getAll(this.getSessionId());
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

  @GetMapping(API_PATH + "/entry")
  public ResponseEntity<String> entry(@NotEmpty @RequestParam String oid)
  {
    JsonObject response = this.service.getEntry(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/versions")
  public ResponseEntity<String> versions(@NotEmpty @RequestParam String oid)
  {
    JsonArray response = this.service.getVersions(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/version")
  public ResponseEntity<String> version(@NotEmpty @RequestParam String oid, @RequestParam(defaultValue = "false", required = false) Boolean includeTableDefinitions)
  {
    JsonObject response = this.service.getVersion(this.getSessionId(), oid, includeTableDefinitions);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/data")
  public ResponseEntity<String> data(@NotEmpty @RequestParam String oid)
  {
    JsonObject response = this.service.getData(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/geo-objects")
  public ResponseEntity<String> geoObjects(@NotEmpty @RequestParam String oid, @NotEmpty @RequestParam Long skip, @NotEmpty @RequestParam Integer blockSize)
  {
    JsonArray response = this.service.getGeoObjects(this.getSessionId(), oid, skip, blockSize);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/edges")
  public ResponseEntity<String> edges(@NotEmpty @RequestParam String oid, @NotEmpty @RequestParam Long skip, @NotEmpty @RequestParam Integer blockSize)
  {
    JsonArray response = this.service.getEdges(this.getSessionId(), oid, skip, blockSize);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove-version")
  public ResponseEntity<Void> removeVersion(@Valid @RequestBody OidBody body)
  {
    this.service.removeVersion(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  // @GetMapping(API_PATH + "/get-publish-jobs")
  // public ResponseEntity<String> getPublishJobs(
  // @NotEmpty @RequestParam String oid,
  // @RequestParam Integer pageSize,
  // @RequestParam Integer pageNumber,
  // @RequestParam(required = false) String sortAttr,
  // @RequestParam(required = false) Boolean isAscending)
  // {
  // if (sortAttr == null || sortAttr.equals(""))
  // {
  // sortAttr = "createDate";
  // }
  //
  // if (isAscending == null)
  // {
  // isAscending = true;
  // }
  //
  // JsonObject config = this.service.getPublishJobs(this.getSessionId(), oid,
  // pageSize, pageNumber, sortAttr, isAscending);
  //
  // return new ResponseEntity<String>(config.toString(), HttpStatus.OK);
  // }
  //
  // @GetMapping(API_PATH + "/get-publish-job")
  // public ResponseEntity<String> getPublishJob(
  // @NotEmpty @RequestParam String historyOid)
  // {
  // JsonObject job = this.service.getPublishJob(this.getSessionId(),
  // historyOid);
  //
  // return new ResponseEntity<String>(job.toString(), HttpStatus.OK);
  // }
}
