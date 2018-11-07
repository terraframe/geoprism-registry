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

import java.io.InputStream;
import java.util.List;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;
import com.runwaysdk.mvc.ViewTemplateResponse;

import net.geoprism.AccessConstants;
import net.geoprism.GeoprismUserDTO;
import net.geoprism.JSONStringImpl;
import net.geoprism.dashboard.layer.DashboardLayerDTO;
import net.geoprism.gis.geoserver.GeoserverProperties;

@Controller(url = "dashboard-map")
public class DashboardMapController
{
  public static final String JSP_DIR = "/WEB-INF/net/geoprism/dashboard/DashboardMap/";

  public static final String LAYOUT  = "WEB-INF/templates/layout.jsp";

  @Endpoint(url = "view", method = ServletMethod.GET)
  public ResponseIF createMapForSession(ClientRequestIF clientRequest)
  {
    // Get all dashboards
    DashboardQueryDTO dashboardQ = DashboardDTO.getSortedDashboards(clientRequest);
    List<? extends DashboardDTO> dashboards = dashboardQ.getResultSet();

    if (dashboards.size() == 0)
    {
      return new ViewTemplateResponse(LAYOUT, JSP_DIR, "nodashboard.jsp");
    }

    ViewResponse response = new ViewResponse("/WEB-INF/net/geoprism/dashboard/DashboardMap/dashboardViewer.jsp");
    response.set("workspace", GeoserverProperties.getWorkspace());
    response.set("editDashboard", GeoprismUserDTO.hasAccess(clientRequest, AccessConstants.EDIT_DASHBOARD));
    response.set("editData", GeoprismUserDTO.hasAccess(clientRequest, AccessConstants.EDIT_DATA));

    return response;
  }

  @Endpoint(url = "export-map", method = ServletMethod.GET)
  public ResponseIF exportMap(ClientRequestIF request, @RequestParamter(name = "mapId") String mapId, @RequestParamter(name = "outFileName") String outFileName, @RequestParamter(name = "outFileFormat") String outFileFormat, @RequestParamter(name = "mapBounds") String mapBounds, @RequestParamter(name = "mapSize") String mapSize, @RequestParamter(name = "activeBaseMap") String activeBaseMap)
  {
    DashboardMapDTO map = DashboardMapDTO.get(request, mapId);

    if (outFileName == null || outFileName.length() == 0)
    {
      outFileName = "default";
    }

    InputStream mapImageInStream = map.generateMapImageExport(outFileFormat, mapBounds, mapSize, activeBaseMap);

    return new InputStreamResponse(mapImageInStream, "application/" + outFileFormat, outFileName);
  }

  @Endpoint(url = "export-layer", method = ServletMethod.GET)
  public ResponseIF exportLayerData(ClientRequestIF request, @RequestParamter(name = "mapId") String mapId, @RequestParamter(name = "state") String state, @RequestParamter(name = "layerId") String layerId)
  {
    DashboardMapDTO map = DashboardMapDTO.get(request, mapId);
    DashboardLayerDTO layer = DashboardLayerDTO.get(request, layerId);
    String layerName = layer.getNameLabel().getValue();

    InputStream istream = map.exportLayerData(state, layerId);

    return new InputStreamResponse(istream, "application/xlsx", layerName);
  }

  @Endpoint(url = "refresh", method = ServletMethod.POST)
  public ResponseIF refresh(ClientRequestIF request, @RequestParamter(name = "mapId") String mapId, @RequestParamter(name = "state") String state)
  {
    String map = DashboardMapDTO.refresh(request, mapId, state);

    RestResponse response = new RestResponse();
    response.set("map", new JSONStringImpl(map));
    response.set("information", request.getInformation());

    return response;
  }

  @Endpoint(url = "order-layers", method = ServletMethod.POST)
  public ResponseIF orderLayers(ClientRequestIF request, @RequestParamter(name = "mapId") String mapId, @RequestParamter(name = "layerIds") String[] layerIds)
  {
    String json = DashboardMapDTO.orderLayers(request, mapId, layerIds);

    return new RestBodyResponse(new JSONStringImpl(json));
  }
}
