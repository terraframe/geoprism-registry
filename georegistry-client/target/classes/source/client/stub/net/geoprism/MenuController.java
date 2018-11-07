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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.system.RolesDTO;
import com.runwaysdk.system.SingleActorDTO;

@Controller(url = "menu")
public class MenuController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF applications(ClientRequestIF request) throws JSONException
  {
    Set<String> roleNames = this.getAssignedRoleNames(request);

    List<GeoprismApplication> allApplications = ClientConfigurationService.getApplications(request);
    // List<GeoprismApplication> authorizedApplications = allApplications.stream().filter(p ->
    // p.isValid(roleNames)).collect(Collectors.toList());

    JSONArray response = new JSONArray();

    for (GeoprismApplication application : allApplications)
    {
      if (application.isValid(roleNames))
      {
        response.put(application.toJSON());
      }
    }

    return new RestBodyResponse(response);
  }

  private Set<String> getAssignedRoleNames(ClientRequestIF request)
  {
    Set<String> roleNames = new TreeSet<String>();

    SingleActorDTO currentUser = GeoprismUserDTO.getCurrentUser(request);

    List<? extends RolesDTO> userRoles = currentUser.getAllAssignedRole();
    for (RolesDTO role : userRoles)
    {
      roleNames.add(role.getRoleName());
    }

    return roleNames;
  }

}
