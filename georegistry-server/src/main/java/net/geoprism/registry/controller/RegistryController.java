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

import org.commongeoregistry.adapter.constants.RegistryUrls;

import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.ViewResponse;

import net.geoprism.registry.GeoregistryProperties;

@Controller(url = RegistryUrls.REGISTRY_CONTROLLER_URL)
public class RegistryController
{
  public static final String JSP_DIR   = "/WEB-INF/";

  public static final String INDEX_JSP = "net/geoprism/registry/index.jsp";

//  @Endpoint(method = ServletMethod.GET)
//  public ResponseIF manage()
//  {
//    ViewResponse resp = new ViewResponse(JSP_DIR + INDEX_JSP);
//
//    String customFont = GeoregistryProperties.getCustomFont();
//    if (customFont != null && customFont.length() > 0)
//    {
//      resp.set("customFont", customFont);
//    }
//
//    return resp;
//  }
  @Endpoint(method = ServletMethod.GET)
  public ResponseIF manage()
  {
    ViewResponse resp = new ViewResponse("/index.html");
    
//    String customFont = GeoregistryProperties.getCustomFont();
//    if (customFont != null && customFont.length() > 0)
//    {
//      resp.set("customFont", customFont);
//    }
    
    return resp;
  }
}
