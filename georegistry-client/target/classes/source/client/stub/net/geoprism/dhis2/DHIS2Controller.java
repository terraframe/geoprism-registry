/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.dhis2;

import org.json.JSONArray;
import org.json.JSONException;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;

import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.PluginUtilDTO;

@Controller(url = "dhis2")
public class DHIS2Controller 
{
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF isEnabled(ClientRequestIF request) throws java.io.IOException, javax.servlet.ServletException, JSONException
  {
    boolean isEnabled = PluginUtilDTO.isDHIS2Enabled(request);
    
    return new RestBodyResponse(isEnabled);
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTrackedEntities(ClientRequestIF request) throws java.io.IOException, javax.servlet.ServletException, JSONException
  {
    String json = DHIS2IdMappingDTO.findTrackedEntityIds(request);
    
    return new RestBodyResponse(new JSONArray(json));
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getPrograms(ClientRequestIF request) throws java.io.IOException, javax.servlet.ServletException, JSONException
  {
    String json = DHIS2IdMappingDTO.findPrograms(request);
    
    return new RestBodyResponse(new JSONArray(json));
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getTrackedEntityAttributes(ClientRequestIF request) throws java.io.IOException, javax.servlet.ServletException, JSONException
  {
    String json = DHIS2IdMappingDTO.findAttributes(request);
    
    return new RestBodyResponse(new JSONArray(json));
  }
}
