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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.BusinessObjectService;

@RestController
@Validated
public class BusinessObjectController extends RunwaySpringController
{
  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "business-object";

  @Autowired
  private BusinessObjectService service;

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get( 
      @NotBlank @RequestParam String businessTypeCode,
      @NotBlank @RequestParam String code)
  {
    JsonObject response = service.get(this.getSessionId(), businessTypeCode, code);
    
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-type-and-object")
  public ResponseEntity<String> getTypeAndObject( 
      @NotBlank @RequestParam String businessTypeCode,
      @NotBlank @RequestParam String code)
  {
    JsonObject response = service.getTypeAndObject(this.getSessionId(), businessTypeCode, code);
    
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/get-parents")
  public ResponseEntity<String> getParents( 
      @NotBlank @RequestParam String businessTypeCode,
      @NotBlank @RequestParam String code,
      @NotBlank @RequestParam String businessEdgeTypeCode)
  {
    JsonArray parents = this.service.getParents(this.getSessionId(), businessTypeCode, code, businessEdgeTypeCode);
    
    return new ResponseEntity<String>(parents.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/get-children")
  public ResponseEntity<String> getChildren( 
      @NotBlank @RequestParam String businessTypeCode,
      @NotBlank @RequestParam String code,
      @NotBlank @RequestParam String businessEdgeTypeCode)
  {
    JsonArray parents = this.service.getChildren(this.getSessionId(), businessTypeCode, code, businessEdgeTypeCode);
    
    return new ResponseEntity<String>(parents.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/get-geo-objects")
  public ResponseEntity<String> getGeoObjects( 
      @NotBlank @RequestParam String businessTypeCode,
      @NotBlank @RequestParam String code,
      @NotBlank @RequestParam String date,
      @NotBlank @RequestParam String edgeTypeCode,
      @NotBlank @RequestParam String direction
)
  {
    JsonArray geoObjects = this.service.getGeoObjects(this.getSessionId(), businessTypeCode, code, date, edgeTypeCode, direction);
    
    return new ResponseEntity<String>(geoObjects.toString(), HttpStatus.OK);
  }
  
}
