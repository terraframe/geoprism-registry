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

import com.google.gson.JsonObject;

import net.geoprism.registry.controller.ETLController.ConfigBody;
import net.geoprism.registry.service.request.CurationService;

@RestController
@Validated
public class CurationController extends RunwaySpringController
{
  public static final String API_PATH = "curation";

  public static class VersionIdBody
  {
    @NotEmpty
    private String listTypeVersionId;

    public String getListTypeVersionId()
    {
      return listTypeVersionId;
    }

    public void setListTypeVersionId(String listTypeVersionId)
    {
      this.listTypeVersionId = listTypeVersionId;
    }
  }

  public static class CurationResolutionBody
  {
    @NotEmpty
    String problemId;

    @NotEmpty
    String resolution;

    public String getProblemId()
    {
      return problemId;
    }

    public void setProblemId(String problemId)
    {
      this.problemId = problemId;
    }

    public String getResolution()
    {
      return resolution;
    }

    public void setResolution(String resolution)
    {
      this.resolution = resolution;
    }
  }

  @Autowired
  protected CurationService service;

  @GetMapping(API_PATH + "/details")  
  public ResponseEntity<String> details(@RequestParam(required = false) String historyId, @RequestParam(required = false) Boolean onlyUnresolved, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    JsonObject details = this.service.details(this.getSessionId(), historyId, onlyUnresolved, pageSize, pageNumber);

    return new ResponseEntity<String>(details.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/page")  
  public ResponseEntity<String> page(@RequestParam(required = false) String historyId, @RequestParam(required = false) Boolean onlyUnresolved, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    JsonObject page = this.service.page(this.getSessionId(), historyId, onlyUnresolved, pageSize, pageNumber);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/curate")    
  public ResponseEntity<String> curate(@Valid @RequestBody VersionIdBody body)
  {
    JsonObject serializedHistory = this.service.curate(this.getSessionId(), body.getListTypeVersionId());

    return new ResponseEntity<String>(serializedHistory.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/problem-resolve")      
  public ResponseEntity<Void> submitProblemResolution(@Valid @RequestBody ConfigBody body)
  {
    this.service.submitProblemResolution(this.getSessionId(), body.config.toString());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/set-resolution")        
  public ResponseEntity<Void> setResolution(@Valid @RequestBody CurationResolutionBody body)
  {
    this.service.setResolution(this.getSessionId(), body.getProblemId(), body.getResolution());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

}
