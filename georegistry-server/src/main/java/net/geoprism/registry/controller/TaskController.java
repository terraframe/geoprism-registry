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

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.task.TaskService;

@Controller(url = "tasks")
public class TaskController
{
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "orderBy") String orderBy, @RequestParamter(name = "pageNum") Integer pageNum, @RequestParamter(name = "pageSize") Integer pageSize, @RequestParamter(name = "whereStatus") String whereStatus) throws JSONException
  {
    if (orderBy == null || orderBy.length() == 0)
    {
      orderBy = "createDate";
    }
    if (pageNum == null || pageNum == 0)
    {
      pageNum = 1;
    }
    if (pageSize == null || pageSize == 0)
    {
      pageSize = Integer.MAX_VALUE;
    }
    
    JSONObject jo = TaskService.getTasksForCurrentUser(request.getSessionId(), orderBy, pageNum, pageSize, whereStatus);
    
    return new RestBodyResponse(jo.toString());
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF delete(ClientRequestIF request, @RequestParamter(name = "id") String id) throws JSONException
  {
    TaskService.deleteTask(request.getSessionId(), id);
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF complete(ClientRequestIF request, @RequestParamter(name = "id") String id) throws JSONException
  {
    TaskService.completeTask(request.getSessionId(), id);
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF setTaskStatus(ClientRequestIF request, @RequestParamter(name = "id") String id, @RequestParamter(name = "status") String status) throws JSONException
  {
    TaskService.setTaskStatus(request.getSessionId(), id, status);
    
    return new RestResponse();
  }
}
