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

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
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
import com.google.gson.JsonObject;

import net.geoprism.registry.controller.GeoObjectController.TypeCodeBody;
import net.geoprism.registry.service.RegistryComponentService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class GeoObjectOverTimeController extends RunwaySpringController
{
  public static class GeoObjectOverTimeBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject geoObject;

    public JsonObject getGeoObject()
    {
      return geoObject;
    }

    public void setGeoObject(JsonObject geoObject)
    {
      this.geoObject = geoObject;
    }
  }
  
  public static final String API_PATH = "geoobject-time";
  
  @Autowired
  private RegistryComponentService service;  
  
  @GetMapping(RegistryUrls.GEO_OBJECT_TIME_GET)  
  public ResponseEntity<String> getGeoObjectOverTime(
      @NotEmpty @RequestParam String id,
      @NotEmpty @RequestParam String typeCode) 
  {
    GeoObjectOverTime geoObject = this.service.getGeoObjectOverTime(this.getSessionId(), id, typeCode);

    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject response = geoObject.toJSON(serializer);
    
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(RegistryUrls.GEO_OBJECT_TIME_GET_CODE)    
  public ResponseEntity<String> getGeoObjectOverTimeByCode(
      @NotEmpty @RequestParam String code, 
      @NotEmpty @RequestParam String typeCode) 
  {
    GeoObjectOverTime geoObject = this.service.getGeoObjectOverTimeByCode(this.getSessionId(), code, typeCode);

    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject response = geoObject.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/newGeoObjectInstance")  
  public ResponseEntity<String> newGeoObjectOverTime(@Valid @RequestBody TypeCodeBody body)
  {
    String response = this.service.newGeoObjectInstanceOverTime(this.getSessionId(), body.getTypeCode());

    return new ResponseEntity<String>(response, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-bounds")  
  public ResponseEntity<String> getGeoObjectBoundsAtDate(
      @NotEmpty @RequestParam String code, 
      @NotEmpty @RequestParam String typeCode,
      @RequestParam(required = false) Date date)
  {
    GeoObject geoObject = this.service.getGeoObjectByCode(this.getSessionId(), code, typeCode, date);

    String bounds = this.service.getGeoObjectBoundsAtDate(this.getSessionId(), geoObject, date);

    return new ResponseEntity<String>(bounds, HttpStatus.OK);
  }

  @PostMapping(RegistryUrls.GEO_OBJECT_TIME_CREATE)    
  public ResponseEntity<String> createGeoObjectOverTime(@Valid @RequestBody GeoObjectOverTimeBody body)
  {
    GeoObjectOverTime geoObject = this.service.createGeoObjectOverTime(this.getSessionId(), body.geoObject.toString());
    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject response = geoObject.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(RegistryUrls.GEO_OBJECT_TIME_UPDATE)      
  public ResponseEntity<String> updateGeoObjectOverTime(@Valid @RequestBody GeoObjectOverTimeBody body)
  {
    GeoObjectOverTime goTime = this.service.updateGeoObjectOverTime(this.getSessionId(), body.geoObject.toString());
    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject response = goTime.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

}
