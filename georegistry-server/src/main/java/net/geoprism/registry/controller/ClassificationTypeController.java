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
import com.google.gson.JsonObject;
import com.runwaysdk.mvc.RequestParamter;

import net.geoprism.registry.controller.BusinessTypeController.OidBody;
import net.geoprism.registry.service.ClassificationTypeService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class ClassificationTypeController extends RunwaySpringController
{
  public static class ClassificationTypeBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject classificationType;
    
    public JsonObject getClassificationType()
    {
      return classificationType;
    }
    
    public void setClassificationType(JsonObject classificationType)
    {
      this.classificationType = classificationType;
    }
  } 
  
  public static final String API_PATH = "classification-type";

  @Autowired
  private ClassificationTypeService service;

  @GetMapping(API_PATH + "/page")
  public ResponseEntity<String> page(@RequestParamter(name = "criteria") String criteria)
  {
    JsonObject response = this.service.page(this.getSessionId(), criteria);
    
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<String> apply(@Valid @RequestBody ClassificationTypeBody body)
  {
    JsonObject response = this.service.apply(this.getSessionId(), body.classificationType.toString());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody OidBody body)
  {
    this.service.remove(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get(@NotNull @RequestParam String classificationCode)
  {
    JsonObject response = this.service.get(this.getSessionId(), classificationCode);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
}
