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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.geoprism.localization.LocalizationFacadeDTO;

import com.runwaysdk.constants.ClientRequestIF;

public class DefaultClientConfiguration implements ClientConfigurationIF
{

  @Override
  public List<GeoprismApplication> getApplications(ClientRequestIF request)
  {
    /*
     * Default applications which geoprism defines
     */
    GeoprismApplication kaleidoscope = new GeoprismApplication();
    kaleidoscope.setId("kaleidoscope");
    kaleidoscope.setLabel(LocalizationFacadeDTO.getFromBundles(request, "geoprismLanding.dashboards"));
    kaleidoscope.setSrc("net/geoprism/images/k_icon.svg");
    kaleidoscope.setUrl("nav/kaleidoscopes");

    GeoprismApplication management = new GeoprismApplication();
    management.setId("management");
    management.setLabel(LocalizationFacadeDTO.getFromBundles(request, "geoprismLanding.dataManagement"));
    management.setSrc("net/geoprism/images/dm_icon.svg");
    management.setUrl("prism/home#data");
    management.addRole(RoleConstants.ADIM_ROLE);
    management.addRole(RoleConstants.BUILDER_ROLE);

    List<GeoprismApplication> applications = new LinkedList<GeoprismApplication>();
    applications.add(kaleidoscope);
    applications.add(management);

    return applications;
  }

  @Override
  public Set<String> getPublicEndpoints()
  {
    TreeSet<String> endpoint = new TreeSet<String>();
    endpoint.add("logo/view");
    endpoint.add("prism/admin");
    endpoint.add("prism/home");
    endpoint.add("forgotpassword/initiate");
    endpoint.add("forgotpassword/complete");

    return endpoint;
  }

}
