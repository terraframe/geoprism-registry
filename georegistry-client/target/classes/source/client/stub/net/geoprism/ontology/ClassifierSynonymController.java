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
package net.geoprism.ontology;

import java.io.IOException;

import org.json.JSONArray;

import com.runwaysdk.business.ontology.TermAndRelDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ErrorUtility;
import com.runwaysdk.system.ontology.TermUtilDTO;
import com.runwaysdk.transport.conversion.json.BusinessDTOToJSON;
import com.runwaysdk.transport.conversion.json.JSONReturnObject;

public class ClassifierSynonymController extends ClassifierSynonymControllerBase 
{
  public static final String JSP_DIR = "/WEB-INF/net/geoprism/ontology/ClassifierSynonym/";

  public static final String LAYOUT  = "WEB-INF/templates/layout.jsp";

  public ClassifierSynonymController(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp, Boolean isAsynchronous)
  {
    super(req, resp, isAsynchronous, JSP_DIR, LAYOUT);
  }

  public void cancel(ClassifierSynonymDTO dto) throws IOException, javax.servlet.ServletException
  {
    dto.unlock();
    this.view(dto.getOid());
  }

  public void failCancel(ClassifierSynonymDTO dto) throws IOException, javax.servlet.ServletException
  {
    this.edit(dto.getOid());
  }

  public void create(ClassifierSynonymDTO dto, String classifierId) throws IOException, javax.servlet.ServletException
  {
    try
    {
      TermAndRelDTO tnr = ClassifierSynonymDTO.createSynonym(this.getClientRequest(), dto, classifierId);

      this.resp.getWriter().print(new JSONReturnObject(tnr.toJSON()).toString());
    }
    catch (Throwable t)
    {
      ErrorUtility.handleFormError(t, req, resp);
    }
  }

  public void failCreate(ClassifierSynonymDTO dto, String classifierId) throws IOException, javax.servlet.ServletException
  {
    req.setAttribute("item", dto);
    render("createComponent.jsp");
  }

  public void failCreate(ClassifierSynonymDTO dto) throws IOException, javax.servlet.ServletException
  {
    req.setAttribute("item", dto);
    render("createComponent.jsp");
  }

  public void delete(ClassifierSynonymDTO dto) throws IOException, javax.servlet.ServletException
  {
    try
    {
      dto.delete();
      this.viewAll();
    }
    catch (com.runwaysdk.ProblemExceptionDTO e)
    {
      this.failDelete(dto);
    }
  }

  public void failDelete(ClassifierSynonymDTO dto) throws IOException, javax.servlet.ServletException
  {
    req.setAttribute("item", dto);
    render("editComponent.jsp");
  }

  public void edit(String oid) throws IOException, javax.servlet.ServletException
  {
    ClassifierSynonymDTO dto = ClassifierSynonymDTO.lock(super.getClientRequest(), oid);
    req.setAttribute("item", dto);
    render("editComponent.jsp");
  }

  public void failEdit(String oid) throws IOException, javax.servlet.ServletException
  {
    this.view(oid);
  }

  public void newInstance() throws IOException, javax.servlet.ServletException
  {
    ClientRequestIF clientRequest = super.getClientRequest();
    ClassifierSynonymDTO dto = new ClassifierSynonymDTO(clientRequest);
    req.setAttribute("item", dto);
    render("createComponent.jsp");
  }

  public void failNewInstance() throws IOException, javax.servlet.ServletException
  {
    this.viewAll();
  }

  public void update(ClassifierSynonymDTO dto) throws IOException, javax.servlet.ServletException
  {
    try
    {
      dto.apply();

      String ret = BusinessDTOToJSON.getConverter(dto).populate().toString();

      this.resp.getWriter().print(new JSONReturnObject(ret).toString());
    }
    catch (Throwable t)
    {
      boolean needsRedirect = ErrorUtility.handleFormError(t, req, resp);

      if (needsRedirect)
      {
        this.viewUpdate(dto.getOid());
      }
    }
  }

  public void failUpdate(ClassifierSynonymDTO dto) throws IOException, javax.servlet.ServletException
  {
    req.setAttribute("item", dto);
    render("editComponent.jsp");
  }

  public void view(String oid) throws IOException, javax.servlet.ServletException
  {
    ClientRequestIF clientRequest = super.getClientRequest();
    req.setAttribute("item", ClassifierSynonymDTO.get(clientRequest, oid));
    render("viewComponent.jsp");
  }

  public void failView(String oid) throws IOException, javax.servlet.ServletException
  {
    this.viewAll();
  }

  public void viewAll() throws IOException, javax.servlet.ServletException
  {
    ClientRequestIF clientRequest = super.getClientRequest();
    ClassifierSynonymQueryDTO query = ClassifierSynonymDTO.getAllInstances(clientRequest, null, true, 20, 1);
    req.setAttribute("query", query);
    render("viewAllComponent.jsp");
  }

  public void failViewAll() throws IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }

  public void viewPage(String sortAttribute, Boolean isAscending, Integer pageSize, Integer pageNumber) throws IOException, javax.servlet.ServletException
  {
    ClientRequestIF clientRequest = super.getClientRequest();
    ClassifierSynonymQueryDTO query = ClassifierSynonymDTO.getAllInstances(clientRequest, sortAttribute, isAscending, pageSize, pageNumber);
    req.setAttribute("query", query);
    render("viewAllComponent.jsp");
  }

  public void failViewPage(String sortAttribute, String isAscending, String pageSize, String pageNumber) throws IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }

  public void getDirectDescendants(String parentOid) throws IOException, javax.servlet.ServletException
  {
    try
    {
      JSONArray array = new JSONArray();

      TermAndRelDTO[] tnrs = TermUtilDTO.getDirectDescendants(getClientRequest(), parentOid, new String[] { ClassifierHasSynonymDTO.CLASS });

      for (TermAndRelDTO tnr : tnrs)
      {
        array.put(tnr.toJSON());
      }

      resp.getWriter().print(new JSONReturnObject(array).toString());
    }
    catch (Throwable t)
    {
      ErrorUtility.prepareAjaxThrowable(t, resp);
    }
  }

  public void failGetDirectDescendants(String parentOid) throws IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }

  public void viewCreate() throws IOException, javax.servlet.ServletException
  {
    ClientRequestIF clientRequest = super.getClientRequest();
    ClassifierSynonymDTO dto = new ClassifierSynonymDTO(clientRequest);
    req.setAttribute("item", dto);
    render("createComponent.jsp");
  }

  public void failViewCreate() throws IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }

  public void viewUpdate(String oid) throws IOException, javax.servlet.ServletException
  {
    ClassifierSynonymDTO dto = ClassifierSynonymDTO.lock(super.getClientRequest(), oid);
    req.setAttribute("item", dto);
    render("editComponent.jsp");
  }

  public void failViewUpdate(String oid) throws IOException, javax.servlet.ServletException
  {
    resp.sendError(500);
  }
}
