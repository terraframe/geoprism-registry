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
package net.geoprism.dashboard.layer;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;

import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.JSONControllerUtil;

@Controller(url = "iconimage")
public class CategoryIconController 
{
  @Endpoint(method = ServletMethod.POST)
  public ResponseIF create(ClientRequestIF request, @RequestParamter(name = "file") MultipartFileParameter file, @RequestParamter(name = "label") String label)
  {
    try
    {
      String filename = file.getFilename();
      InputStream stream = file.getInputStream();

      try
      {
        String result = CategoryIconDTO.create(request, filename, stream, label);

        return new RestBodyResponse(new JSONArray(result));
      }
      finally
      {
        /*
         * Just in case the stream isn't closed by the server method
         */
        stream.close();
      }
    }
    catch (Throwable t)
    {
      return JSONControllerUtil.handleException(t);
    }
  }

  @Endpoint(method = ServletMethod.POST)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "file") MultipartFileParameter file, @RequestParamter(name = "label") String label)
  {
    try
    {
      CategoryIconDTO icon = CategoryIconDTO.get(request, oid);
      icon.getDisplayLabel().setValue(label);

      if (file != null)
      {
        String filename = file.getFilename();
        InputStream stream = file.getInputStream();

        try
        {
          icon.applyWithFile(filename, stream);
        }
        finally
        {
          /*
           * Just in case the stream isn't closed by the server method
           */
          stream.close();
        }
      }
      else
      {
        icon.apply();
      }

      return new RestBodyResponse(new JSONObject(icon.getAsJSON()));
    }
    catch (Throwable t)
    {
      return JSONControllerUtil.handleException(t);
    }
  }

  @Endpoint(method = ServletMethod.POST)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    try
    {
      CategoryIconDTO icon = CategoryIconDTO.lock(request, oid);

      return new RestBodyResponse(new JSONObject(icon.getAsJSON()));
    }
    catch (Throwable t)
    {
      return JSONControllerUtil.handleException(t);
    }
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    CategoryIconDTO.unlock(request, oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getAll(ClientRequestIF request) throws JSONException
  {
    String icons = CategoryIconDTO.getAllAsJSON(request);

    JSONObject object = new JSONObject();
    object.put("icons", new JSONArray(icons));

    return new RestBodyResponse(object);
  }

  @Endpoint(method = ServletMethod.POST)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    try
    {
      CategoryIconDTO.remove(request, oid);

      return new RestBodyResponse("");
    }
    catch (Throwable t)
    {
      return JSONControllerUtil.handleException(t);
    }
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF getCategoryIconImage(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    CategoryIconDTO icon = CategoryIconDTO.get(request, oid);

    return new InputStreamResponse(icon.getIcon(), "image/png");
  }
}
