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
package net.geoprism.data.importer;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import net.geoprism.localization.LocalizationFacadeDTO;
import net.geoprism.report.PairViewDTO;
import net.geoprism.report.ReportItemDTO;

import org.apache.commons.io.IOUtils;

import com.runwaysdk.controller.ErrorUtility;
import com.runwaysdk.controller.MultipartFileParameter;

public class ExcelController extends ExcelControllerBase 
{
  public static final String JSP_DIR = "/WEB-INF/net/geoprism/data/importer/Excel/";

  public static final String LAYOUT  = "/WEB-INF/templates/layout.jsp";

  public ExcelController(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp, java.lang.Boolean isAsynchronous)
  {
    super(req, resp, isAsynchronous, JSP_DIR, LAYOUT);
  }

  @Override
  public void exportExcelForm(String type) throws IOException, ServletException
  {
    PairViewDTO[] countries = ReportItemDTO.getGeoEntitySuggestions(this.getClientRequest(), "", 0);

    this.req.setAttribute("countries", countries);
    this.req.setAttribute("type", type);

    this.render("exportForm.jsp");
  }

  @Override
  public void failExportExcelForm(String type) throws IOException, ServletException
  {
    resp.sendError(500);
  }

  @Override
  public void exportExcelFile(String type, String country, String downloadToken) throws IOException, ServletException
  {

    try
    {
      // The reason we're including a cookie here is because the browser does not give us any indication of when our
      // response from the server is successful and its downloading the file.
      // This "hack" sends a downloadToken to the client, which the client then checks for the existence of every so
      // often. When the cookie exists, it knows its downloading it.
      // http://stackoverflow.com/questions/1106377/detect-when-browser-receives-file-download
      Cookie cookie = new Cookie("downloadToken", downloadToken);
      cookie.setMaxAge(10 * 60); // 10 minute cookie expiration
      resp.addCookie(cookie);

      InputStream istream = ExcelUtilDTO.exportExcelFile(this.getClientRequest(), type, country);

      try
      {
        // copy it to response's OutputStream
        this.resp.setContentType("application/xlsx");
        this.resp.setHeader("Content-Disposition", "attachment; filename=\"template.xlsx\"");

        IOUtils.copy(istream, this.resp.getOutputStream());

        this.resp.flushBuffer();
      }
      finally
      {
        istream.close();
      }
    }
    catch (RuntimeException e)
    {
      if (!resp.isCommitted())
      {
        resp.reset();
      }

      ErrorUtility.prepareThrowable(e, req, resp, false, true);
    }
  }

  @Override
  public void excelImportForm() throws IOException, ServletException
  {
    PairViewDTO[] countries = ReportItemDTO.getGeoEntitySuggestions(this.getClientRequest(), "", 0);

    this.req.setAttribute("countries", countries);

    this.render("importForm.jsp");
  }

  @Override
  public void failExcelImportForm() throws IOException, ServletException
  {
    resp.sendError(500);
  }

  @Override
  public void importExcelFile(MultipartFileParameter file, String country, String downloadToken) throws IOException, ServletException
  {
    // The reason we're including a cookie here is because the browser does not give us any indication of when our
    // response from the server is successful and its downloading the file.
    // This "hack" sends a downloadToken to the client, which the client then checks for the existence of every so
    // often. When the cookie exists, it knows its downloading it.
    // http://stackoverflow.com/questions/1106377/detect-when-browser-receives-file-download

    Cookie cookie = new Cookie("downloadToken", downloadToken);
    cookie.setMaxAge(10 * 60); // 10 minute cookie expiration
    resp.addCookie(cookie);

    try
    {
      if (file == null)
      {
        throw new RuntimeException(LocalizationFacadeDTO.getFromBundles(this.getClientRequest(), "file.required"));
      }

      InputStream istream = file.getInputStream();

      try
      {
        InputStream result = ExcelUtilDTO.importExcelFile(this.getClientRequest(), istream, country);

        if (result != null)
        {
          // copy it to response's OutputStream
          this.resp.setContentType("application/xlsx");
          this.resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFilename() + "\"");

          IOUtils.copy(result, this.resp.getOutputStream());
        }
        else
        {
          this.resp.getWriter().print("<p oid=\"upload_result\" class=\"success\"></p>");
        }
        
        this.resp.flushBuffer();
      }
      finally
      {
        istream.close();
      }
    }
    catch (Throwable t)
    {
      this.resp.getWriter().print("<p oid=\"upload_result\" class=\"error\">" + t.getLocalizedMessage() + "</p>");
    }
  }
}
