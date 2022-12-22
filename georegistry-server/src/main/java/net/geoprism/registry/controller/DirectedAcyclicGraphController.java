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

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
import com.runwaysdk.mvc.RequestParamter;

import net.geoprism.registry.service.DirectedAcyclicGraphService;
import net.geoprism.registry.spring.NullableDateDeserializer;

@RestController
@Validated
public class DirectedAcyclicGraphController extends RunwaySpringController
{
  public static final class DagRequestBody
  {
    @NotEmpty
    String parentCode;

    @NotEmpty
    String parentTypeCode;

    @NotEmpty
    String childCode;

    @NotEmpty
    String childTypeCode;

    @NotEmpty
    String directedGraphCode;

    @NotNull
    @JsonDeserialize(using = NullableDateDeserializer.class)
    Date   startDate;

    @NotNull
    @JsonDeserialize(using = NullableDateDeserializer.class)
    Date   endDate;

    public String getParentCode()
    {
      return parentCode;
    }

    public void setParentCode(String parentCode)
    {
      this.parentCode = parentCode;
    }

    public String getParentTypeCode()
    {
      return parentTypeCode;
    }

    public void setParentTypeCode(String parentTypeCode)
    {
      this.parentTypeCode = parentTypeCode;
    }

    public String getChildCode()
    {
      return childCode;
    }

    public void setChildCode(String childCode)
    {
      this.childCode = childCode;
    }

    public String getChildTypeCode()
    {
      return childTypeCode;
    }

    public void setChildTypeCode(String childTypeCode)
    {
      this.childTypeCode = childTypeCode;
    }

    public String getDirectedGraphCode()
    {
      return directedGraphCode;
    }

    public void setDirectedGraphCode(String directedGraphCode)
    {
      this.directedGraphCode = directedGraphCode;
    }

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
    }
  }

  public static final String API_PATH = "dag";

  @Autowired
  private DirectedAcyclicGraphService service;

  @GetMapping(API_PATH + "/get-children")
  public ResponseEntity<String> getChildren(@RequestParam String parentCode, @RequestParam String parentTypeCode, @RequestParamter(name = "directedGraphCode", required = true) String directedGraphCode, @RequestParam Boolean recursive, @RequestParam Date date)
  {
    JsonObject response = this.service.getChildren(this.getSessionId(), parentCode, parentTypeCode, directedGraphCode, recursive, date);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-parents")
  public ResponseEntity<String> getParents(@RequestParam String childCode, @RequestParam String childTypeCode, @RequestParam String directedGraphCode, @RequestParam Boolean recursive, @RequestParam Date date)
  {
    JsonObject response = this.service.getParents(this.getSessionId(), childCode, childTypeCode, directedGraphCode, recursive, date);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/add-child")
  public ResponseEntity<String> addChild(@Valid
  @RequestBody DagRequestBody body)
  {
    JsonObject response = this.service.addChild(this.getSessionId(), body.parentCode, body.parentTypeCode, body.childCode, body.childTypeCode, body.directedGraphCode, body.startDate, body.endDate);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove-child")
  public ResponseEntity<Void> removeChild(@Valid
  @RequestBody DagRequestBody body)
  {
    this.service.removeChild(this.getSessionId(), body.parentCode, body.parentTypeCode, body.childCode, body.childTypeCode, body.directedGraphCode, body.startDate, body.endDate);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }
}
