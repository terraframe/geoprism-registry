/*******************************************************************************
 * Copyright (C) 2018 IVCC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
package net.geoprism.dhis2.dhis2adapter.response;

import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HTTPResponse
{
  private String response;
  
  private int statusCode;
  
  public HTTPResponse(String response, int statusCode)
  {
    this.response = response;
    this.statusCode = statusCode;
  }

  public JsonObject getJsonObject()
  {
    return JsonParser.parseString(response).getAsJsonObject();
  }
  
  public JsonArray getJsonArray()
  {
    return JsonParser.parseString(response).getAsJsonArray();
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
  
  public boolean isSuccess()
  {
    final int[] successCodes = new int[] {200, 201, 202, 203, 204, 205, 206, 207, 208, 226};
    
    boolean isSuccessCode = Arrays.stream(successCodes).anyMatch(new Integer(this.getStatusCode())::equals);
    
    if (this.response == null)
    {
      return isSuccessCode;
    }
    
    JsonObject jo = this.getJsonObject();
    
    return isSuccessCode && !this.hasErrorMessage() && !jo.get("status").getAsString().equals("ERROR");
  }
  
  public String getErrorMessage()
  {
//    {"httpStatus":"Conflict","httpStatusCode":409,"status":"ERROR","message":"Unexpected end-of-input: expected close marker for Array (start marker at [Source: (BufferedInputStream); line: 1, column: 22])\n at [Source: (BufferedInputStream); line: 1, column: 45]"}

    if (this.hasErrorMessage())
    {
      return this.getJsonObject().get("message").getAsString();
    }
    else
    {
      return "";
    }
  }
  
  public boolean hasErrorMessage()
  {
    JsonObject jo = this.getJsonObject();
    
    if (this.getStatusCode() != 200 || jo.has("status") && jo.get("status").getAsString().equals("ERROR"))
    {
      return jo.has("message");
    }
    
    return false;
  }
}
