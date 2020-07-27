/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.dhis2.dhis2adapter.response;

import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class DHIS2Response
{
  protected String response;
  
  protected int statusCode;
  
  public DHIS2Response(String response, int statusCode)
  {
    this.response = response;
    this.statusCode = statusCode;
  }
  
  public boolean isSuccess()
  {
    final int[] successCodes = new int[] {200, 201, 202, 203, 204, 205, 206, 207, 208, 226};
    
    boolean isSuccessCode = Arrays.stream(successCodes).anyMatch(new Integer(this.getStatusCode())::equals);
    
    return isSuccessCode;
  }

  public JsonObject getJsonObject()
  {
    if (this.response == null)
    {
      return null;
    }
    
    try
    {
      return JsonParser.parseString(response).getAsJsonObject();
    }
    catch (JsonParseException | IllegalStateException e)
    {
      return null;
    }
  }
  
  public JsonArray getJsonArray()
  {
    if (this.response == null)
    {
      return null;
    }
    
    try
    {
      return JsonParser.parseString(response).getAsJsonArray();
    }
    catch (JsonParseException e)
    {
      return null;
    }
  }

  public String getResponse()
  {
    return this.response;
  }
  
  public void setResponse(String response)
  {
    this.response = response;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
}
