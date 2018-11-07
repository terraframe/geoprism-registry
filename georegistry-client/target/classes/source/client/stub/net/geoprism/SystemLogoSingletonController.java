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

import com.runwaysdk.controller.MultipartFileParameter;

public class SystemLogoSingletonController extends SystemLogoSingletonControllerBase 
{
  public static final String JSP_DIR = "/WEB-INF/net/geoprism/SystemLogoSingleton/";

  public static final String LAYOUT  = "/WEB-INF/templates/layout.jsp";

  public SystemLogoSingletonController(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp, java.lang.Boolean isAsynchronous)
  {
    super(req, resp, isAsynchronous);
  }
  
  public void uploadBanner(MultipartFileParameter banner) throws java.io.IOException, javax.servlet.ServletException
  {
    if (banner != null)
    {
      SystemLogoSingletonDTO.uploadBannerAndCache(this.getClientRequest(), banner.getInputStream(), banner.getFilename());
    }
    new AdminController(req, resp, false).system();
  }
  
  public void failUploadBanner(MultipartFileParameter banner) throws java.io.IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }
  
  public void uploadMiniLogo(MultipartFileParameter miniLogo) throws java.io.IOException, javax.servlet.ServletException
  {
    if (miniLogo != null)
    {
      SystemLogoSingletonDTO.uploadMiniLogoAndCache(this.getClientRequest(), miniLogo.getInputStream(), miniLogo.getFilename());
    }
    new AdminController(req, resp, false).system();
  }
  
  public void failUploadMiniLogo(MultipartFileParameter miniLogo) throws java.io.IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }
  
  public void viewUploadBanner() throws java.io.IOException, javax.servlet.ServletException
  {
//    File bannerFile = SystemLogoSingletonDTO.getBannerFileFromCache(this.getClientRequest());
//    if (bannerFile != null)
//    {
//      this.req.setAttribute("bannerFilePath", bannerFile.getAbsolutePath());
//      this.req.setAttribute("bannerFileName", bannerFile.getName());
//    }
//    
//    render("viewUploadBanner.jsp");
  }
  
  public void failViewUploadBanner() throws java.io.IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }
  
  public void viewUploadMiniLogo() throws java.io.IOException, javax.servlet.ServletException
  {
//    File miniLogoFile = SystemLogoSingletonDTO.getMiniLogoFileFromCache(this.getClientRequest());
//    if (miniLogoFile != null)
//    {
//      this.req.setAttribute("miniLogoFilePath", miniLogoFile.getAbsolutePath());
//      this.req.setAttribute("miniLogoFileName", miniLogoFile.getName());
//    }
//    
//    render("viewUploadMiniLogo.jsp");
  }
  
  public void failViewUploadMiniLogo() throws java.io.IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }
}
