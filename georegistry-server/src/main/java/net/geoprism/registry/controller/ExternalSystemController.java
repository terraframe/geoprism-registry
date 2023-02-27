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

import java.io.File;
import java.io.FileNotFoundException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
import com.google.gson.JsonObject;

import net.geoprism.registry.controller.BusinessTypeController.OidBody;
import net.geoprism.registry.dhis2.DHIS2PluginZipManager;
import net.geoprism.registry.service.ExternalSystemService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class ExternalSystemController extends RunwaySpringController
{
  public static final String API_PATH = "external-system";

  public static final class SystemBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject system;

    public JsonObject getSystem()
    {
      return system;
    }

    public void setSystem(JsonObject system)
    {
      this.system = system;
    }
  }

  @Autowired
  private ExternalSystemService service;

  @GetMapping(API_PATH + "/system-capabilities")
  public ResponseEntity<String> getSystemCapabilities(@NotEmpty
  @RequestParam String system)
  {
    JsonObject response = this.service.getSystemCapabilities(this.getSessionId(), system);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/download-dhis2-plugin")
  public ResponseEntity<FileSystemResource> downloadDhis2Plugin() throws FileNotFoundException
  {
    File pluginZip = DHIS2PluginZipManager.getPluginZip();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.valueOf("application/zip"));
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cgr-dhis2-app.zip");

    return new ResponseEntity<FileSystemResource>(new FileSystemResource(pluginZip), headers, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> page(@RequestParam Integer pageNumber, @RequestParam Integer pageSize)
  {
    JsonObject response = this.service.page(this.getSessionId(), pageNumber, pageSize);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<String> apply(@Valid @RequestBody SystemBody body)
  {
    JsonObject response = this.service.apply(this.getSessionId(), body.system);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<Void> remove(@RequestBody OidBody body)
  {
    this.service.remove(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get(@NotEmpty
  @RequestParam String oid)
  {
    JsonObject response = this.service.get(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
}
