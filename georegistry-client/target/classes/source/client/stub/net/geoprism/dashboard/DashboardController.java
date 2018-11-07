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
package net.geoprism.dashboard;

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.business.ValueQueryDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ParseType;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.JSONStringImpl;

@Controller(url = "dashboard-controller")
public class DashboardController
{
  // public ResponseIF create(DashboardDTO dto)
  // {
  // try
  // {
  // DashboardDTO applied = DashboardDTO.create(getClientRequest(), dto);
  //
  // JSONReturnObject jsonReturn = new JSONReturnObject(applied);
  // jsonReturn.setInformation(this.getClientRequest().getInformation());
  // jsonReturn.setWarnings(this.getClientRequest().getWarnings());
  //
  // this.getResponse().setStatus(200);
  // this.getResponse().setContentType("application/json");
  //
  // this.getResponse().getWriter().print(jsonReturn.toString());
  // }
  // catch (com.runwaysdk.ProblemExceptionDTO e)
  // {
  // this.failCreate(dto);
  // }
  // }

  @Endpoint(url = "new-clone", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newClone(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId) throws JSONException
  {
    DashboardDTO dashboard = DashboardDTO.get(request, dashboardId);

    JSONObject object = new JSONObject();
    object.put("label", dashboard.getDisplayLabel().getValue());
    object.put("oid", dashboard.getOid());

    JSONObject response = new JSONObject();
    response.put("dashboard", object);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "clone", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF clone(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId, @RequestParamter(name = "label") String label) throws JSONException
  {
    DashboardDTO dashboard = DashboardDTO.clone(request, dashboardId, label);

    JSONObject response = new JSONObject(dashboard.getDashboardInformation());
    response.put("oid", dashboard.getOid());
    response.put("isLastDashboard", true);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "available-dashboards", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF availableDashboards(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId) throws JSONException
  {
    String json = DashboardDTO.getAvailableDashboardsAsJSON(request, dashboardId);

    return new RestBodyResponse(new JSONStringImpl(json));
  }

  @Endpoint(url = "dashboard-definition", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF dashboardDefinition(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId) throws JSONException
  {
    DashboardDTO dashboard = null;

    if (dashboardId != null && dashboardId.length() > 0)
    {
      dashboard = DashboardDTO.lock(request, dashboardId);
    }
    else
    {
      dashboard = new DashboardDTO(request);
    }

    String json = dashboard.getDashboardDefinition();

    RestResponse response = new RestResponse();
    response.set("definition", new JSONStringImpl(json));
    response.set("dto", dashboard);

    return response;
  }

  @Endpoint(url = "layers-to-delete", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF layersToDelete(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId, @RequestParamter(name = "options") String options) throws JSONException
  {
    String json = DashboardDTO.getLayersToDelete(request, dashboardId, options);

    return new RestBodyResponse(new JSONStringImpl(json));
  }

  @Endpoint(url = "apply-with-options", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF applyWithOptions(ClientRequestIF request, @RequestParamter(parser = ParseType.BASIC_JSON, name = "dto") DashboardDTO dto, @RequestParamter(name = "options") String options) throws JSONException
  {
    String json = dto.applyWithOptions(options);

    return new RestBodyResponse(new JSONStringImpl(json));
  }

  @Endpoint(url = "unlock", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId) throws JSONException
  {
    DashboardDTO.unlock(request, dashboardId);

    return new RestResponse();
  }

  @Endpoint(url = "base-layer-state", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF setBaseLayerState(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId, @RequestParamter(name = "baseLayerState") String baseLayerState) throws JSONException
  {
    DashboardDTO.setBaseLayerState(request, dashboardId, baseLayerState);

    return new RestResponse();
  }

  @Endpoint(url = "save-state", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF saveState(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId, @RequestParamter(name = "state") String state, @RequestParamter(name = "global") Boolean global) throws JSONException
  {
    String json = DashboardDTO.saveState(request, dashboardId, state, global);

    return new RestBodyResponse(new JSONStringImpl(json));
  }

  @Endpoint(url = "get-json", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getJSON(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId) throws JSONException
  {
    String json = DashboardDTO.getJSON(request, dashboardId);

    return new RestBodyResponse(new JSONStringImpl(json));
  }

  @Endpoint(url = "has-report", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF hasReport(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId) throws JSONException
  {
    Boolean response = DashboardDTO.hasReport(request, dashboardId);

    return new RestBodyResponse(response);
  }

  @Endpoint(url = "geo-suggestions", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF geoSuggestions(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId, @RequestParamter(name = "text") String text, @RequestParamter(name = "limit") Integer limit) throws JSONException
  {
    ValueQueryDTO query = DashboardDTO.getGeoEntitySuggestions(request, dashboardId, text, limit);

    return new RestBodyResponse(query);
  }

  @Endpoint(url = "text-suggestions", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF textSuggestions(ClientRequestIF request, @RequestParamter(name = "mdAttributeId") String mdAttributeId, @RequestParamter(name = "text") String text, @RequestParamter(name = "limit") Integer limit) throws JSONException
  {
    String[] results = DashboardDTO.getTextSuggestions(request, mdAttributeId, text, limit);

    return new RestBodyResponse(results);
  }

  @Endpoint(url = "category-suggestions", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF classifierSuggestions(ClientRequestIF request, @RequestParamter(name = "mdAttributeId") String mdAttributeId, @RequestParamter(name = "geoNodeId") String geoNodeId, @RequestParamter(name = "universalId") String universalId, @RequestParamter(name = "aggregationVal") String aggregationVal, @RequestParamter(name = "state") String state, @RequestParamter(name = "text") String text, @RequestParamter(name = "limit") Integer limit) throws JSONException
  {
    String[] results = DashboardDTO.getCategoryInputSuggestions(request, mdAttributeId, geoNodeId, universalId, aggregationVal, text, limit, state);

    return new RestBodyResponse(results);
  }

  @Endpoint(url = "remove", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId)
  {
    request.delete(dashboardId);

    return new RestResponse();
  }

  @Endpoint(url = "generate-thumbnail", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF generateThumbnail(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId)
  {
    DashboardDTO.generateThumbnailImage(request, dashboardId);

    return new RestResponse();
  }

  @Endpoint(url = "set-dataset-order", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF setDatasetOrder(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId, @RequestParamter(name = "typeIds") String[] typeIds)
  {
    DashboardDTO.setMetadataWrapperOrder(request, dashboardId, typeIds);

    return new RestResponse();
  }

  @Endpoint(url = "set-attribute-order", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF setAttributeOrder(ClientRequestIF request, @RequestParamter(name = "dashboardId") String dashboardId, @RequestParamter(name = "typeId") String typeId, @RequestParamter(name = "attributeIds") String[] attributeIds)
  {
    DashboardDTO.setDashboardAttributesOrder(request, dashboardId, typeId, attributeIds);

    return new RestResponse();
  }

  @Endpoint(url = "classifier-tree", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getClassifierTree(ClientRequestIF request, @RequestParamter(name = "mdAttributeId") String mdAttributeId) throws JSONException
  {
    String json = DashboardDTO.getClassifierTree(request, mdAttributeId);

    return new RestBodyResponse(new JSONStringImpl(json));
  }
}
