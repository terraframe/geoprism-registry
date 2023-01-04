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

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;

import net.geoprism.registry.service.RelationshipVisualizationService;
import net.geoprism.registry.visualization.VertexView;

@RestController
@Validated
public class RelationshipVisualizationController extends RunwaySpringController
{
  public static final String API_PATH = "relationship-visualization";

  @Autowired
  private RelationshipVisualizationService service;


  @GetMapping(API_PATH + "/tree")
  public ResponseEntity<String> tree( 
      @NotEmpty @RequestParam String relationshipType,
      @NotEmpty @RequestParam String graphTypeCode,
      @NotEmpty @RequestParam String sourceVertex, 
      @RequestParam(required = false) Date date, 
      @RequestParam(required = false) String boundsWKT)
  {
    JsonElement json = this.service.tree(this.getSessionId(), date, relationshipType, graphTypeCode, sourceVertex, boundsWKT);

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/treeAsGeoJson")
  public ResponseEntity<String> treeAsGeoJson(
      @NotEmpty @RequestParam String relationshipType,
      @NotEmpty @RequestParam String graphTypeCode,
      @NotEmpty @RequestParam String sourceVertex, 
      @RequestParam(required = false) Date date, 
      @RequestParam(required = false) String boundsWKT)
  {
    JsonElement json = this.service.treeAsGeoJson(this.getSessionId(), date, relationshipType, graphTypeCode, sourceVertex, boundsWKT);

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }
  
  @GetMapping(API_PATH + "/relationships")
  public ResponseEntity<String> relationships(
      @NotEmpty @RequestParam(name = "objectType") String objectType, 
      @NotEmpty @RequestParam(name = "typeCode") String typeCode)
  {
    JsonElement json = this.service.getRelationshipTypes(this.getSessionId(), VertexView.ObjectType.valueOf(objectType), typeCode);

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }
}
