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

import net.geoprism.registry.service.TaskService;

@RestController
@Validated
public class TaskController extends RunwaySpringController
{
  public static final String  API_PATH = "tasks";
  
  public static class IdBody 
  {
    @NotEmpty 
    private String id;
    
    public String getId()
    {
      return id;
    }
    
    public void setId(String id)
    {
      this.id = id;
    }
  }

  public static class TaskStatusBody extends IdBody
  {
    @NotEmpty
    private String status;
    
    public String getStatus()
    {
      return status;
    }
    
    public void setStatus(String status)
    {
      this.status = status;
    }
  }
  
  @Autowired
  private TaskService service;
  
  
  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get(
      @RequestParam(required = false, defaultValue = "createDate") String orderBy, 
      @RequestParam(required = false, defaultValue = "1") Integer pageNum, 
      @RequestParam(required = false, defaultValue = "1000") Integer pageSize, 
      @RequestParam(required = false) String whereStatus)
  {
    JsonObject jo = service.getTasksForCurrentUser(this.getSessionId(), orderBy, pageNum, pageSize, whereStatus);

    return new ResponseEntity<String>(jo.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/delete")  
  public ResponseEntity<Void> delete(@Valid @RequestBody IdBody body)
  {
    service.deleteTask(this.getSessionId(), body.getId());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/complete")  
  public ResponseEntity<Void> complete(@Valid @RequestBody IdBody body)
  {
    service.completeTask(this.getSessionId(), body.getId());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/setTaskStatus")  
  public ResponseEntity<Void> setTaskStatus(@Valid @RequestBody TaskStatusBody body)
  {
    service.setTaskStatus(this.getSessionId(), body.getId(), body.getStatus());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }
}
