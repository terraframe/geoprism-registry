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
package net.geoprism;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.mvc.ErrorRestResponse;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.transport.conversion.json.JSONExceptionDTO;
import com.runwaysdk.transport.conversion.json.JSONReturnObject;
import com.runwaysdk.web.json.JSONRunwayExceptionDTO;

public class JSONControllerUtil
{
  public static void writeReponse(HttpServletResponse resp) throws IOException
  {
    JSONReturnObject ret = new JSONReturnObject();
    ret.setReturnValue("");

    String content = ret.getJSON().toString();
    byte[] bytes = content.getBytes("UTF-8");

    resp.setStatus(200);
    resp.setContentType("application/json");

    JSONControllerUtil.writeOutputStream(resp, bytes);
  }

  public static void writeReponse(HttpServletResponse resp, Object returnValue) throws IOException
  {
    JSONReturnObject ret = new JSONReturnObject();
    ret.setReturnValue(returnValue);

    String content = ret.getJSON().toString();
    byte[] bytes = content.getBytes("UTF-8");

    resp.setStatus(200);
    resp.setContentType("application/json");

    JSONControllerUtil.writeOutputStream(resp, bytes);
  }

  public static ResponseIF writeReponse(Object returnValue)
  {
//    JSONReturnObject ret = new JSONReturnObject();
//    ret.setReturnValue(returnValue);

    return new RestBodyResponse(returnValue);
  }

  public static void handleException(HttpServletResponse resp, Throwable t, ClientRequestIF request) throws IOException
  {
    JSONRunwayExceptionDTO ex = new JSONRunwayExceptionDTO(t);

    JSONControllerUtil.writeException(resp, ex);
  }

  public static ResponseIF handleException(Throwable t)
  {
    JSONRunwayExceptionDTO ex = new JSONRunwayExceptionDTO(t);

    return new ErrorRestResponse(ex.getJSON());
  }

  public static void writeException(HttpServletResponse resp, JSONExceptionDTO ex) throws IOException
  {
    String content = ex.getJSON();

    resp.setStatus(500);
    resp.setContentType("application/json");

    writeOutputStream(resp, content.getBytes("UTF-8"));
  }

  public static void writeOutputStream(HttpServletResponse resp, byte[] bytes) throws IOException
  {
    ServletOutputStream ostream = resp.getOutputStream();

    try
    {
      ostream.write(bytes);
      ostream.flush();
    }
    finally
    {
      ostream.close();
    }
  }
}
