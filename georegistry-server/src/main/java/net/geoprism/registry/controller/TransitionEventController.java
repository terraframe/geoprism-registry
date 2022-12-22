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

import java.io.IOException;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;

import net.geoprism.registry.service.TransitionEventService;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class TransitionEventController extends RunwaySpringController
{
  public static class EventIdBody 
  {
    @NotEmpty
    private String eventId;
    
    public String getEventId()
    {
      return eventId;
    }
    
    public void setEventId(String eventId)
    {
      this.eventId = eventId;
    }
  }
  
  public static class EventBody 
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject event;
    
    public JsonObject getEvent()
    {
      return event;
    }
    
    public void setEvent(JsonObject event)
    {
      this.event = event;
    }
  }
  
  public static final String API_PATH = "transition-event";

  @Autowired
  private TransitionEventService service;

  @GetMapping(API_PATH + "/page")
  public ResponseEntity<String> page( 
      @RequestParam Integer pageSize,
      @RequestParam Integer pageNumber, 
      @RequestParam(required = false) String attrConditions)
  {
    JsonObject response = service.page(this.getSessionId(), pageSize, pageNumber, attrConditions);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-details")  
  public ResponseEntity<String> getDetails( @NotEmpty @RequestParam String oid)
  {
    JsonObject response = service.getDetails(this.getSessionId(), oid);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")    
  public ResponseEntity<String> apply( @Valid @RequestBody EventBody body)
  {
    JsonObject response = this.service.apply(this.getSessionId(), body.event);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/delete")      
  public ResponseEntity<Void> delete( @Valid @RequestBody EventIdBody body)
  {
    this.service.delete(this.getSessionId(), body.getEventId());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/historical-report")    
  public ResponseEntity<String> getHistoricalReport( 
      @NotEmpty @RequestParam String typeCode, 
      @RequestParam(required = false) Date startDate, 
      @RequestParam(required = false) Date endDate, 
      @RequestParam(required = false) Integer pageSize, 
      @RequestParam(required = false) Integer pageNumber) 
  {
    JsonObject response = service.getHistoricalReport(this.getSessionId(), typeCode, startDate, endDate, pageSize, pageNumber);
    
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/export-excel")      
  public ResponseEntity<InputStreamResource> exportExcel( 
      @NotEmpty @RequestParam String typeCode, 
      @RequestParam(required = false) Date startDate, 
      @RequestParam(required = false) Date endDate) throws IOException
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historical-report.xlsx");

    InputStreamResource isr = new InputStreamResource(service.exportExcel(this.getSessionId(), typeCode, startDate, endDate));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }

}
