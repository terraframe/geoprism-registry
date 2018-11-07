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

@com.runwaysdk.business.ClassSignature(hash = 65312103)
public class DataUploaderControllerBase 
{
  public static final String CLASS = "net.geoprism.DataUploaderController";
  protected javax.servlet.http.HttpServletRequest req;
  protected javax.servlet.http.HttpServletResponse resp;
  protected java.lang.Boolean isAsynchronous;
  protected java.lang.String dir;
  protected java.lang.String layout;
  
  public DataUploaderControllerBase(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp, java.lang.Boolean isAsynchronous)
  {
    this(req, resp, isAsynchronous, "","");
  }
  
  public DataUploaderControllerBase(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp, java.lang.Boolean isAsynchronous, java.lang.String dir, java.lang.String layout)
  {
    this.req = req;
    this.resp = resp;
    this.isAsynchronous = isAsynchronous;
    this.dir = dir;
    this.layout = layout;
  }
  
  protected void render(String jsp) throws java.io.IOException, javax.servlet.ServletException
  {
    if(!resp.isCommitted())
    {
      if(this.isAsynchronous())
      {
        req.getRequestDispatcher(dir+jsp).forward(req, resp);
      }
      else
      {
        req.setAttribute(com.runwaysdk.controller.JSPFetcher.INNER_JSP, dir+jsp);
        req.getRequestDispatcher(layout).forward(req, resp);
      }
    }
  }
  
  public javax.servlet.http.HttpServletRequest getRequest()
  {
    return this.req;
  }
  
  public javax.servlet.http.HttpServletResponse getResponse()
  {
    return this.resp;
  }
  
  public java.lang.Boolean isAsynchronous()
  {
    return this.isAsynchronous;
  }
  
  public com.runwaysdk.constants.ClientRequestIF getClientRequest()
  {
    return (com.runwaysdk.constants.ClientRequestIF) req.getAttribute(com.runwaysdk.constants.ClientConstants.CLIENTREQUEST);
  }
  
  public com.runwaysdk.ClientSession getClientSession()
  {
    return (com.runwaysdk.ClientSession) req.getSession().getAttribute(com.runwaysdk.constants.ClientConstants.CLIENTSESSION);
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:configuration", post=true)
  public void cancelImport(java.lang.String configuration) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.cancelImport");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:configuration", post=true)
  public void failCancelImport(java.lang.String configuration) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failCancelImport");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:classifierId, java.lang.String:label", post=true)
  public void createClassifierSynonym(java.lang.String classifierId, java.lang.String label) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.createClassifierSynonym");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:classifierId, java.lang.String:label", post=true)
  public void failCreateClassifierSynonym(java.lang.String classifierId, java.lang.String label) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failCreateClassifierSynonym");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:parentOid, java.lang.String:universalId, java.lang.String:label", post=true)
  public void createGeoEntity(java.lang.String parentOid, java.lang.String universalId, java.lang.String label) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.createGeoEntity");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:parentOid, java.lang.String:universalId, java.lang.String:label", post=true)
  public void failCreateGeoEntity(java.lang.String parentOid, java.lang.String universalId, java.lang.String label) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failCreateGeoEntity");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:entityId, java.lang.String:label", post=true)
  public void createGeoEntitySynonym(java.lang.String entityId, java.lang.String label) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.createGeoEntitySynonym");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:entityId, java.lang.String:label", post=true)
  public void failCreateGeoEntitySynonym(java.lang.String entityId, java.lang.String label) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failCreateGeoEntitySynonym");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:synonymId", post=true)
  public void deleteClassifierSynonym(java.lang.String synonymId) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.deleteClassifierSynonym");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:synonymId", post=true)
  public void failDeleteClassifierSynonym(java.lang.String synonymId) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failDeleteClassifierSynonym");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:entityId", post=true)
  public void deleteGeoEntity(java.lang.String entityId) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.deleteGeoEntity");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:entityId", post=true)
  public void failDeleteGeoEntity(java.lang.String entityId) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failDeleteGeoEntity");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:synonymId", post=true)
  public void deleteGeoEntitySynonym(java.lang.String synonymId) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.deleteGeoEntitySynonym");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:synonymId", post=true)
  public void failDeleteGeoEntitySynonym(java.lang.String synonymId) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failDeleteGeoEntitySynonym");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="com.runwaysdk.controller.MultipartFileParameter:file", post=true)
  public void getAttributeInformation(com.runwaysdk.controller.MultipartFileParameter file) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.getAttributeInformation");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="com.runwaysdk.controller.MultipartFileParameter:file", post=true)
  public void failGetAttributeInformation(com.runwaysdk.controller.MultipartFileParameter file) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failGetAttributeInformation");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:mdAttributeId, java.lang.String:text, java.lang.Integer:limit", post=false)
  public void getClassifierSuggestions(java.lang.String mdAttributeId, java.lang.String text, java.lang.Integer limit) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.getClassifierSuggestions");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:mdAttributeId, java.lang.String:text, java.lang.String:limit", post=false)
  public void failGetClassifierSuggestions(java.lang.String mdAttributeId, java.lang.String text, java.lang.String limit) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failGetClassifierSuggestions");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:oid, java.lang.String:sheetName", post=true)
  public void getSavedConfiguration(java.lang.String oid, java.lang.String sheetName) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.getSavedConfiguration");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:oid, java.lang.String:sheetName", post=true)
  public void failGetSavedConfiguration(java.lang.String oid, java.lang.String sheetName) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failGetSavedConfiguration");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:configuration", post=true)
  public void importData(java.lang.String configuration) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.importData");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:configuration", post=true)
  public void failImportData(java.lang.String configuration) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failImportData");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:name, java.lang.String:oid", post=false)
  public void validateCategoryName(java.lang.String name, java.lang.String oid) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.validateCategoryName");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:name, java.lang.String:oid", post=false)
  public void failValidateCategoryName(java.lang.String name, java.lang.String oid) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failValidateCategoryName");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:name, java.lang.String:oid", post=false)
  public void validateDatasetName(java.lang.String name, java.lang.String oid) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.validateDatasetName");
  }
  
  @com.runwaysdk.controller.ActionParameters(parameters="java.lang.String:name, java.lang.String:oid", post=false)
  public void failValidateDatasetName(java.lang.String name, java.lang.String oid) throws java.io.IOException, javax.servlet.ServletException
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.DataUploaderController.java";
    throw new com.runwaysdk.controller.UndefinedControllerActionException(msg, req.getLocale(), "net.geoprism.DataUploaderController.failValidateDatasetName");
  }
  
}
