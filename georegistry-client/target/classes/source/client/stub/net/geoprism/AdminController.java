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

import javax.servlet.ServletException;

import net.geoprism.AccessConstants;
import net.geoprism.AdminControllerBase;
import net.geoprism.CachedImageUtil;
import net.geoprism.ontology.ClassifierDTO;
import net.geoprism.ontology.ClassifierIsARelationshipDTO;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.system.gis.geo.AllowedInDTO;
import com.runwaysdk.system.gis.geo.GeoEntityDTO;
import com.runwaysdk.system.gis.geo.IsARelationshipDTO;
import com.runwaysdk.system.gis.geo.LocatedInDTO;
import com.runwaysdk.system.gis.geo.UniversalDTO;
import com.runwaysdk.web.json.JSONController;

public class AdminController extends AdminControllerBase 
{
  public static final String JSP_DIR   = "/WEB-INF/net/geoprism/admin/";

  public static final String LAYOUT    = "/WEB-INF/templates/layout.jsp";

  public static final String INDEX_JSP = "/net/geoprism/jsp/index.jsp";

  public AdminController(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp, java.lang.Boolean isAsynchronous)
  {
    super(req, resp, isAsynchronous, JSP_DIR, LAYOUT);
  }

  @Override
  public void users() throws IOException, ServletException
  {
    render("useraccounts.jsp");
  }

  @Override
  public void failUsers() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void geoentity() throws IOException, ServletException
  {
    ClientRequestIF request = this.getClientRequest();

    GeoEntityDTO root = GeoEntityDTO.getRoot(request);

    this.req.setAttribute("type", GeoEntityDTO.CLASS);
    this.req.setAttribute("relationshipType", LocatedInDTO.CLASS);
    this.req.setAttribute("rootId", root.getOid());

    render("geoentity.jsp");
  }

  @Override
  public void failGeoentity() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void roles() throws IOException, ServletException
  {
    render("roles.jsp");
  }

  @Override
  public void failRoles() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void universal() throws IOException, ServletException
  {
    UniversalDTO root = UniversalDTO.getRoot(this.getClientRequest());

    this.req.setAttribute("type", UniversalDTO.CLASS);
    this.req.setAttribute("allowedInType", AllowedInDTO.CLASS);
    this.req.setAttribute("isARelationshipType", IsARelationshipDTO.CLASS);
    this.req.setAttribute("rootId", root.getOid());

    render("universal.jsp");
  }

  @Override
  public void failUniversal() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void scheduler() throws IOException, ServletException
  {
    render("scheduler.jsp");
  }

  @Override
  public void failScheduler() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void databrowser() throws IOException, ServletException
  {
    String sessionId = this.getClientSession().getSessionId();
    String metadata = "{className:'net.geoprism.data.browser.DataBrowserUtil', methodName:'getDefaultTypes', declaredTypes: []}";
    String response = JSONController.invokeMethod(sessionId, metadata, null, "[]");

    this.req.setAttribute("response", response);
    this.req.setAttribute("editData", GeoprismUserDTO.hasAccess(this.getClientRequest(), AccessConstants.EDIT_DATA));

    render("databrowser.jsp");
  }

  @Override
  public void failDatabrowser() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void account() throws IOException, ServletException
  {
    render("account.jsp");
  }

  @Override
  public void failAccount() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void ontologies() throws java.io.IOException, javax.servlet.ServletException
  {
    ClassifierDTO root = ClassifierDTO.getRoot(getClientRequest());

    this.req.setAttribute("type", ClassifierDTO.CLASS);
    this.req.setAttribute("relationshipType", ClassifierIsARelationshipDTO.CLASS);
    this.req.setAttribute("rootId", root.getOid());

    render("ontologies.jsp");
  }

  @Override
  public void failOntologies() throws java.io.IOException, javax.servlet.ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void system() throws java.io.IOException, javax.servlet.ServletException
  {
    String sessionId = this.getClientSession().getSessionId();

    String metadataES = "{className:" + EmailSettingDTO.CLASS + ", methodName:'getDefault', declaredTypes: []}";
    String serializedES = JSONController.invokeMethod(sessionId, metadataES, null, "[]");
    this.req.setAttribute("emailSetting", serializedES);

    String metadataUsr = "{className:" + GeoprismUserDTO.CLASS + ", methodName:'getCurrentUser', declaredTypes: []}";
    String serializedUsr = JSONController.invokeMethod(sessionId, metadataUsr, null, "[]");
    this.req.setAttribute("user", serializedUsr);

    String bannerFile = SystemLogoSingletonDTO.getBannerFileFromCache(this.getClientRequest(), this.req);
    if (bannerFile != null)
    {
      this.req.setAttribute("bannerFilePath", bannerFile);
      this.req.setAttribute("bannerFileName", bannerFile.replaceFirst(SystemLogoSingletonDTO.getImagesTempDir(this.req), ""));
    }
    String miniLogoFile = SystemLogoSingletonDTO.getMiniLogoFileFromCache(this.getClientRequest(), this.req);
    if (miniLogoFile != null)
    {
      this.req.setAttribute("miniLogoFilePath", miniLogoFile);
      this.req.setAttribute("miniLogoFileName", miniLogoFile.replaceFirst(SystemLogoSingletonDTO.getImagesTempDir(this.req), ""));
    }

    render("system.jsp");
  }

  @Override
  public void failSystem() throws java.io.IOException, javax.servlet.ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }

  @Override
  public void datasets() throws IOException, ServletException
  {
    render("datasets.jsp");
  }

  @Override
  public void failDatasets() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }
  
  
  @Override
  public void icons() throws IOException, ServletException
  {
    render("icons.jsp");
  }
  
  @Override
  public void failIcons() throws IOException, ServletException
  {
    this.req.getRequestDispatcher(INDEX_JSP).forward(req, resp);
  }  

  @Override
  public void main() throws IOException, ServletException
  {
//    CachedImageUtil.setBannerPath(this.req, this.resp);

    this.req.getRequestDispatcher("/jsp/mainContent.jsp").forward(this.req, this.resp);
  }
}
