/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.model.DataSourceDTO;
import net.geoprism.registry.service.request.CommitService;
import net.geoprism.registry.view.CommitDTO;

@RestController
@Validated
public class CommitController extends RunwaySpringController
{
  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "commit";

  @Autowired
  private CommitService      service;

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<List<CommitDTO>> getAll(@RequestParam(name = "publishId") String publishId)
  {
    List<CommitDTO> dtos = this.service.getAll(this.getSessionId(), publishId);

    return new ResponseEntity<List<CommitDTO>>(dtos, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-latest")
  public ResponseEntity<CommitDTO> getLatest(@RequestParam(name = "publishId") String publishId)
  {
    CommitDTO dto = this.service.getLatest(this.getSessionId(), publishId);

    return new ResponseEntity<CommitDTO>(dto, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-dependencies")
  public ResponseEntity<List<CommitDTO>> getDependencies(@RequestParam(name = "uid") String uid)
  {
    List<CommitDTO> dependencies = this.service.getDependencies(this.getSessionId(), uid);

    return ResponseEntity.ok(dependencies);
  }

  @GetMapping(API_PATH + "/business-types")
  public ResponseEntity<String> getBusinessTypes(@RequestParam(name = "uid") String uid)
  {
    JsonArray response = this.service.getBusinessTypes(this.getSessionId(), uid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/geo-object-types")
  public ResponseEntity<String> getGeoObjectTypes(@RequestParam(name = "uid") String uid)
  {
    JsonArray response = this.service.getGeoObjectTypes(this.getSessionId(), uid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/business-edge-types")
  public ResponseEntity<String> getBusinessEdgeTypes(@RequestParam(name = "uid") String uid)
  {
    JsonArray response = this.service.getBusinessEdgeTypes(this.getSessionId(), uid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/hierarchy-types")
  public ResponseEntity<String> getHierarchyTypes(@RequestParam(name = "uid") String uid)
  {
    JsonArray response = this.service.getHierarchyTypes(this.getSessionId(), uid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/directed-acyclic-graph-types")
  public ResponseEntity<String> getDirectedAcyclicGraphTypes(@RequestParam(name = "uid") String uid)
  {
    JsonArray response = this.service.getDirectedAcyclicGraphTypes(this.getSessionId(), uid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/undirected-graph-types")
  public ResponseEntity<String> getUndirectedGraphTypes(@RequestParam(name = "uid") String uid)
  {
    JsonArray response = this.service.getUndirectedGraphTypes(this.getSessionId(), uid);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/sources")
  public ResponseEntity<List<DataSourceDTO>> getSources(@RequestParam(name = "uid") String uid)
  {
    List<DataSourceDTO> sources = this.service.getSources(this.getSessionId(), uid);

    return ResponseEntity.ok(sources);
  }

  @GetMapping(API_PATH + "/events")
  public ResponseEntity<List<RemoteEvent>> getEvents(@RequestParam(name = "uid") String uid, @RequestParam(name = "chunk") Integer chunk)
  {
    List<RemoteEvent> events = this.service.getRemoteEvents(this.getSessionId(), uid, chunk);

    return new ResponseEntity<List<RemoteEvent>>(events, HttpStatus.OK);
  }
}
