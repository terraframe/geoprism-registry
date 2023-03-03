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

import net.geoprism.registry.service.UndirectedGraphService;
import net.geoprism.registry.spring.NullableDateDeserializer;

@RestController
@Validated
public class UndirectedGraphController extends RunwaySpringController
{
  public static final class GraphRequestBody
  {
    @NotEmpty
    String sourceCode;

    @NotEmpty
    String sourceTypeCode;

    @NotEmpty
    String targetCode;

    @NotEmpty
    String targetTypeCode;

    @NotEmpty
    String undirectedRelationshipCode;

    @NotNull
    @JsonDeserialize(using = NullableDateDeserializer.class)
    Date   startDate;

    @NotNull
    @JsonDeserialize(using = NullableDateDeserializer.class)
    Date   endDate;

    public String getSourceCode()
    {
      return sourceCode;
    }

    public void setSourceCode(String sourceCode)
    {
      this.sourceCode = sourceCode;
    }

    public String getSourceTypeCode()
    {
      return sourceTypeCode;
    }

    public void setSourceTypeCode(String sourceTypeCode)
    {
      this.sourceTypeCode = sourceTypeCode;
    }

    public String getTargetCode()
    {
      return targetCode;
    }

    public void setTargetCode(String targetCode)
    {
      this.targetCode = targetCode;
    }

    public String getTargetTypeCode()
    {
      return targetTypeCode;
    }

    public void setTargetTypeCode(String targetTypeCode)
    {
      this.targetTypeCode = targetTypeCode;
    }

    public String getUndirectedRelationshipCode()
    {
      return undirectedRelationshipCode;
    }

    public void setUndirectedRelationshipCode(String undirectedRelationshipCode)
    {
      this.undirectedRelationshipCode = undirectedRelationshipCode;
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

  public static final String API_PATH = "undirected";

  @Autowired
  private UndirectedGraphService service;

  @GetMapping(API_PATH + "/get-related-geo-objects")
  public ResponseEntity<String> getChildren(@NotEmpty @RequestParam String sourceCode, @NotEmpty @RequestParam String sourceTypeCode, @NotEmpty @RequestParam String undirectedRelationshipCode, @RequestParam Boolean recursive, @RequestParam Date date)
  {
    JsonObject response = this.service.getChildren(this.getSessionId(), sourceCode, sourceTypeCode, undirectedRelationshipCode, recursive, date);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/add-target")
  public ResponseEntity<String> addChild(@Valid @RequestBody GraphRequestBody body)
  {
    JsonObject response = this.service.addChild(this.getSessionId(), body.sourceCode, body.sourceTypeCode, body.targetCode, body.targetTypeCode, body.undirectedRelationshipCode, body.startDate, body.endDate);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove-target")
  public ResponseEntity<Void> removeChild(@Valid @RequestBody GraphRequestBody body)
  {
    this.service.removeChild(this.getSessionId(), body.sourceCode, body.sourceTypeCode, body.targetCode, body.targetTypeCode, body.undirectedRelationshipCode, body.startDate, body.endDate);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }
}
