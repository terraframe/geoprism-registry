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
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.geoprism.registry.service.request.LocationService;
import net.geoprism.registry.service.request.RegistryComponentService;

/**
 * This controller is used by the location manager widget.
 * 
 * @author rrowlands
 *
 */
@RestController
@Validated
public class RegistryLocationController extends RunwaySpringController
{
  public static final String API_PATH = "registrylocation";

  @Autowired
  private LocationService    service;

  @Autowired
  private RegistryComponentService    registryService;
  
  /**
   * @param request
   * @param code
   *          Code of the geo object being selected
   * @param typeCode
   *          Type code of the geo object being selected
   * @param date
   *          Date in which to use for values and parent lookup
   * @param childTypeCode
   *          Type code of the desired children geo objects
   * @param hierarchyCode
   *          Hierarchy code of the desired children geo objects
   * @return
   * @throws JSONException
   * @throws ParseException
   */

  @GetMapping(API_PATH + "/roots")
  public ResponseEntity<String> roots(
      @RequestParam(required = false) Date date, 
      @RequestParam(required = false) String typeCode, 
      @RequestParam(required = false) String hierarchyCode)
  {
    // ServerGeoObjectIF parent = service.getGeoObjectByEntityId(oid);

    JsonElement json = service.getLocationInformationJson(this.getSessionId(), date, typeCode, hierarchyCode);

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/search")
  public ResponseEntity<String> search(
      @RequestParam(required = false) String text, 
      @RequestParam(required = false) Date date)
  {
    List<GeoObject> results = service.search(this.getSessionId(), text, date);
    CustomSerializer serializer = registryService.serializer(this.getSessionId());

    JsonArray features = results.stream().collect(() -> new JsonArray(), (array, element) -> array.add(element.toJSON(serializer)), (listA, listB) -> {
    });

    JsonObject featureCollection = new JsonObject();
    featureCollection.addProperty("type", "FeatureCollection");
    featureCollection.add("features", features);

    return new ResponseEntity<String>(featureCollection.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/labels")
  public ResponseEntity<String> labels(
      @RequestParam(required = false) String text, 
      @RequestParam(required = false) Date date)
  {
    List<JsonObject> results = service.labels(this.getSessionId(), text, date);
    
    JsonArray response = results.stream().collect(() -> new JsonArray(), (array, element) -> array.add(element), (listA, listB) -> {
    });
    
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
}
