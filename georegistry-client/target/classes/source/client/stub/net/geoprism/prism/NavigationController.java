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
package net.geoprism.prism;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.ViewResponse;
import com.runwaysdk.request.ServletRequestIF;
import com.runwaysdk.system.RolesDTO;
import com.runwaysdk.system.SingleActorDTO;

import net.geoprism.ClientConfigurationService;
import net.geoprism.GeoprismApplication;
import net.geoprism.GeoprismUserDTO;
import net.geoprism.RoleConstants;
import net.geoprism.dashboard.DashboardDTO;
import net.geoprism.dashboard.DashboardQueryDTO;

@Controller(url = "nav")
public class NavigationController
{
  public static final String JSP_DIR         = "/WEB-INF/";

  public static final String LAYOUT          = "WEB-INF/templates/basicLayout.jsp";

  public static final String MENU            = "net/geoprism/userMenu/userMenu.jsp";

  public static final String DATA_MANAGEMENT = "net/geoprism/userMenu/data-management.jsp";

  public static final String DASHBOARDS      = "net/geoprism/userMenu/userDashboards.jsp";

  @Endpoint(url = "management", method = ServletMethod.GET)
  public ResponseIF dataManagement()
  {
    return new ViewResponse(JSP_DIR, DATA_MANAGEMENT);
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF kaleidoscopes(ClientRequestIF clientRequest, ServletRequestIF request)
  {
    DashboardQueryDTO dashboardsQ = DashboardDTO.getSortedDashboards(clientRequest);

    List<? extends DashboardDTO> dashboards = dashboardsQ.getResultSet();

    Set<String> roleNames = this.getAssignedRoleNames(clientRequest);

    ViewResponse response = new ViewResponse(JSP_DIR, DASHBOARDS);
    response.set("dashboards", dashboards);
    response.set("isAdmin", roleNames.contains(RoleConstants.ADIM_ROLE));

    return response;
  }

  /**
   * Gets the dashboard thumbnail for display in the app.
   * 
   * @dashboardId
   */
  @Endpoint(url = "dashboard-thumbnail", method = ServletMethod.GET)
  public ResponseIF getDashboardMapThumbnail(ClientRequestIF clientRequest, @RequestParamter(name = "dashboardId") String dashboardId)
  {
    DashboardDTO db = DashboardDTO.get(clientRequest, dashboardId);

    InputStream istream = db.getThumbnailStream();

    return new InputStreamResponse(istream, "image/png");
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF menu(ClientRequestIF clientRequest, ServletRequestIF request)
  {
    ViewResponse response = new ViewResponse(MENU);

    Set<String> roleNames = this.getAssignedRoleNames(clientRequest);

    List<GeoprismApplication> allApplications = ClientConfigurationService.getApplications(clientRequest);
    List<GeoprismApplication> authorizedApplications = allApplications.stream().filter(p -> p.isValid(roleNames)).collect(Collectors.toList());

    response.set("applications", authorizedApplications);
    response.set("isAdmin", roleNames.contains(RoleConstants.ADIM_ROLE));
    response.set("isBuilder", roleNames.contains(RoleConstants.BUILDER_ROLE));

    return response;
  }

  private Set<String> getAssignedRoleNames(ClientRequestIF clientRequest)
  {
    Set<String> roleNames = new TreeSet<String>();

    SingleActorDTO currentUser = GeoprismUserDTO.getCurrentUser(clientRequest);

    List<? extends RolesDTO> userRoles = currentUser.getAllAssignedRole();
    for (RolesDTO role : userRoles)
    {
      roleNames.add(role.getRoleName());
    }

    return roleNames;
  }

}
