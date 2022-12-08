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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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
import net.geoprism.registry.dhis2.DHIS2FeatureService;
import net.geoprism.registry.etl.fhir.FhirFactory;
import net.geoprism.registry.service.SynchronizationConfigService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class SynchronizationConfigController extends RunwaySpringController
{
  public static final String API_PATH = "synchronization-config";
  
  public static class ConfigBody 
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject config;  
    
    public JsonObject getConfig()
    {
      return config;
    }
    
    public void setConfig(JsonObject config)
    {
      this.config = config;
    }
  }
  
  public static class OptionalOidBody
  {
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


  @Autowired
  private SynchronizationConfigService service;

  @GetMapping(API_PATH + "/get-config-for-es")
  public ResponseEntity<String> getConfigForExternalSystem( 
      @NotEmpty @RequestParam String externalSystemId,
      @NotEmpty @RequestParam String hierarchyTypeCode)
  {
    JsonObject resp = this.service.getConfigForExternalSystem(this.getSessionId(), externalSystemId, hierarchyTypeCode);

    return new ResponseEntity<String>(resp.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-custom-attr")  
  public ResponseEntity<String> getCustomAttributeConfiguration( 
      @NotEmpty @RequestParam String geoObjectTypeCode, 
      @NotEmpty @RequestParam String externalId)
  {
    JsonArray resp = new DHIS2FeatureService().getDHIS2AttributeConfiguration(this.getSessionId(), externalId, geoObjectTypeCode);

    return new ResponseEntity<String>(resp.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-all")    
  public ResponseEntity<String> getAll( 
      @RequestParam Integer pageNumber, 
      @RequestParam Integer pageSize)
  {
    JsonObject response = this.service.page(this.getSessionId(), pageNumber, pageSize);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")      
  public ResponseEntity<String> apply( @Valid @RequestBody ConfigBody body)
  {
    JsonObject response = this.service.apply(this.getSessionId(), body.config);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")        
  public ResponseEntity<Void> remove( @Valid @RequestBody OidBody body)
  {
    this.service.remove(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get")    
  public ResponseEntity<String> get( @NotEmpty @RequestParam String oid)
  {
    JsonObject response = this.service.get(this.getSessionId(), oid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/edit")          
  public ResponseEntity<String> edit( @Valid @RequestBody OptionalOidBody body)
  {
    JsonElement response = this.service.edit(this.getSessionId(), body.getOid());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/unlock")          
  public ResponseEntity<Void> unlock( @Valid @RequestBody OidBody body)
  {
    this.service.unlock(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-jobs")      
  public ResponseEntity<String> getJobs( 
      @NotEmpty @RequestParam String oid, 
      @RequestParam Integer pageNumber,
      @RequestParam Integer pageSize)
  {
    JsonObject jobs = this.service.getJobs(this.getSessionId(), oid, pageSize, pageNumber);

    return new ResponseEntity<String>(jobs.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/run")            
  public ResponseEntity<Void> run( @Valid @RequestBody OidBody body)
  {
    this.service.run(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/generate-file")        
  public ResponseEntity<InputStreamResource> generateFile( @NotEmpty @RequestParam String oid)
  {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "application/zip");
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bundles.zip");

    InputStreamResource isr = new InputStreamResource(this.service.generateFile(this.getSessionId(), oid));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-fhir-export-implementations")          
  public ResponseEntity<String> getFhirExportImplementations()
  {
    JsonArray implementations = FhirFactory.getExportImplementations();

    return new ResponseEntity<String>(implementations.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-fhir-import-implementations")            
  public ResponseEntity<String> getFhirImportImplementations()
  {
    JsonArray implementations = FhirFactory.getImportImplementations();

    return new ResponseEntity<String>(implementations.toString(), HttpStatus.OK);
  }
}
