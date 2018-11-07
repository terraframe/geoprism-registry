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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.LocalProperties;
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

@Controller(url = "logo")
public class SystemLogoController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException
  {
    if (oid != null && oid.equals("banner"))
    {
      SystemLogoSingletonDTO.uploadBannerAndCache(request, file.getInputStream(), file.getFilename());
    }
    else
    {
      SystemLogoSingletonDTO.uploadMiniLogoAndCache(request, file.getInputStream(), file.getFilename());
    }

    return new RestBodyResponse("");
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getAll(ClientRequestIF request) throws JSONException
  {
    JSONObject banner = new JSONObject();
    banner.put("oid", "banner");
    banner.put("label", "Banner");
    banner.put("custom", SystemLogoSingletonDTO.getBannerFileFromCache(request, null) != null);

    JSONObject logo = new JSONObject();
    logo.put("oid", "logo");
    logo.put("label", "Logo");
    logo.put("custom", SystemLogoSingletonDTO.getMiniLogoFileFromCache(request, null) != null);

    JSONArray icons = new JSONArray();
    icons.put(banner);
    icons.put(logo);

    JSONObject object = new JSONObject();
    object.put("icons", icons);

    return new RestBodyResponse(object);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF view(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws IOException
  {
    String path = null;

    if (oid != null && oid.equals("banner"))
    {
      path = SystemLogoSingletonDTO.getBannerFileFromCache(request, null);

      if (path == null)
      {
        path = LocalProperties.getJspDir() + "/../net/geoprism/images/splash_logo.png";
      }
    }
    else
    {
      path = SystemLogoSingletonDTO.getMiniLogoFileFromCache(request, null);

      if (path == null)
      {
        path = LocalProperties.getJspDir() + "/../net/geoprism/images/splash_logo_icon.png";
      }
    }

    File file = new File(path);
    FileInputStream fis = new FileInputStream(file);

    String ext = FilenameUtils.getExtension(file.getName());

    return new InputStreamResponse(fis, "image/" + ext);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws IOException
  {
    if (oid != null && oid.equals("banner"))
    {
      SystemLogoSingletonDTO.removeBannerFileFromCache(request, null);
    }
    else
    {
      SystemLogoSingletonDTO.removeMiniLogoFileFromCache(request, null);
    }

    return new RestResponse();
  }

}
