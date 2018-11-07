/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Runway SDK(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

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
import com.runwaysdk.mvc.ViewResponse;
import com.runwaysdk.request.ServletRequestIF;

import net.geoprism.localization.LocalizationFacadeDTO;

@Controller(url = "dashboard-report")
public class ReportItemController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF upload(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException
  {
    ReportItemDTO report = ReportItemDTO.lockOrCreateReport(request, dashboardId);

    if (report.isNewInstance())
    {
      report.setValue(ReportItemDTO.DASHBOARD, dashboardId);
    }

    if (file != null)
    {
      report.getReportLabel().setValue(file.getFilename());
      report.setReportName(file.getFilename());
      report.applyWithFile(file.getInputStream());
    }
    else
    {
      report.applyWithFile(null);
    }

    return new RestBodyResponse(report);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId) throws JSONException
  {
    ReportItemDTO report = ReportItemDTO.lockOrCreateReport(request, dashboardId);
    report.setValue(ReportItemDTO.DASHBOARD, dashboardId);

    return new RestBodyResponse(report.toJSON());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    ReportItemDTO.unlock(request, oid);
    
    return new RestResponse();
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF run(ClientRequestIF request, ServletRequestIF req, @RequestParamter(name = "report") String report, @RequestParamter(name = "configuration") String configuration) throws JSONException, IOException
  {
    ReportItemDTO item = ReportItemDTO.getReportItemForDashboard(request, report);

    return this.run(request, req, item, configuration);
  }

  private ResponseIF run(ClientRequestIF request, ServletRequestIF req, ReportItemDTO item, String configuration) throws JSONException, IOException
  {
    if (item != null)
    {
      /*
       * First validate permissions, this must be done before
       * response.getOutputStream() is called otherwise redirecting on the error
       * case will not work
       */
      item.validatePermissions();

      String reportUrl = this.getReportURL(req);
      String format = null;

      JSONObject json = new JSONObject(configuration);

      List<ReportParameterDTO> parameters = new LinkedList<ReportParameterDTO>();

      JSONArray jsonArray = json.getJSONArray("parameters");

      for (int i = 0; i < jsonArray.length(); i++)
      {
        JSONObject object = jsonArray.getJSONObject(i);

        String name = object.getString("name");
        String value = object.getString("value");

        parameters.add(this.createReportParameter(request, name, value));

        if (name.equals("format"))
        {
          format = value;
        }
      }

      /*
       * Important: Calling resp.getOutputStream() changes the state of the HTTP
       * request and response objects. However, if an error occurs while
       * rendering the report we need to delegate to the standard error handling
       * mechanism. As such we can't call resp.getOutputStream() until we are
       * sure the report has rendered. Therefore, first render the report to a
       * temp byte array stream. Once that has rendered, copy the bytes from the
       * byte array to the servlet output stream. Note, this may cause memory
       * problems if the report being rendered is too big.
       */
      ByteArrayOutputStream rStream = new ByteArrayOutputStream();

      try
      {
        String url = req.getRequestURL().toString();
        String baseURL = url.substring(0, url.lastIndexOf("/"));

        item.render(rStream, parameters.toArray(new ReportParameterDTO[parameters.size()]), baseURL, reportUrl);

        if (format == null || format.equalsIgnoreCase("html"))
        {
          req.setAttribute("report", rStream.toString());

          ViewResponse response = new ViewResponse("/WEB-INF/net/geoprism/report/report.jsp");
          response.set("report", rStream.toString());
          return response;
        }
        else
        {
          String fileName = item.getReportLabel().getValue().replaceAll("\\s", "_");

          return new InputStreamResponse(new ByteArrayInputStream(rStream.toByteArray()), "application/" + format, fileName + "." + format);
        }
      }
      finally
      {
        rStream.close();
      }
    }
    else
    {
      RestResponse response = new RestResponse();
      response.set("report", LocalizationFacadeDTO.getFromBundles(request, "report.empty"));
      return response;
    }
  }

  private ReportParameterDTO createReportParameter(ClientRequestIF request, String parameterName, String parameterValue)
  {
    ReportParameterDTO parameter = new ReportParameterDTO(request);
    parameter.setParameterName(parameterName);
    parameter.setParameterValue(parameterValue);

    return parameter;
  }

  public String getReportURL(ServletRequestIF req) throws UnsupportedEncodingException
  {
    String str = "dashboard-report/generate?";
    boolean isFirst = true;

    Enumeration<String> paramNames = req.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = paramNames.nextElement();

      if (!paramName.equals("pageNumber"))
      {
        if (!isFirst)
        {
          str = str + "&";
        }

        String[] paramValues = req.getParameterValues(paramName);

        for (int i = 0; i < paramValues.length; i++)
        {
          String paramValue = paramValues[i];
          str = str + URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(paramValue, "UTF-8");
        }

        isFirst = false;
      }
    }
    return str;
  }

  @Endpoint(url = "remove", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId)
  {
    ReportItemDTO item = ReportItemDTO.getReportItemForDashboard(request, dashboardId);
    item.delete();

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF download(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId)
  {
    ReportItemDTO report = ReportItemDTO.getReportItemForDashboard(request, dashboardId);

    if (report != null)
    {
      String reportId = report.getOid();
      String reportName = report.getReportLabel().getValue().replaceAll("\\s", "_");

      InputStream rrStream = ReportItemDTO.getDesignAsStream(request, reportId);

      return new InputStreamResponse(rrStream, "application/" + "octet-stream", reportName + ".rptdesign");
    }

    return new RestResponse();
  }
}
