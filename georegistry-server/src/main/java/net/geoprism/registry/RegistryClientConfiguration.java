/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.runwaysdk.constants.ClientRequestIF;

import net.geoprism.ClientConfigurationIF;
import net.geoprism.DefaultClientConfiguration;
import net.geoprism.GeoprismApplication;
import net.geoprism.RoleConstants;
import net.geoprism.localization.LocalizationFacadeDTO;

public class RegistryClientConfiguration extends DefaultClientConfiguration implements ClientConfigurationIF
{
  @Override
  public List<GeoprismApplication> getApplications(ClientRequestIF request)
  {
    List<GeoprismApplication> applications = new LinkedList<GeoprismApplication>();
    
//    boolean hasSRA = false;
//    JSONArray jaRoles = new JSONArray(RoleViewDTO.getCurrentRoles(request));
//    for (int i = 0; i < jaRoles.length(); ++i)
//    {
//      String roleName = jaRoles.getString(i);
//      
//      if (roleName.equals(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE))
//      {
//        hasSRA = true;
//      }
//    }
//    if (hasSRA)
//    {
//      GeoprismApplication tasks = new GeoprismApplication();
//      tasks.setId("tasks");
//      tasks.setLabel(LocalizationFacadeDTO.getFromBundles(request, "header.tasks"));
//      tasks.setSrc("net/geoprism/images/task.svg");
//      tasks.setUrl("cgr/manage#/registry/tasks");
//      tasks.setDescription(LocalizationFacadeDTO.getFromBundles(request, "header.tasks"));
//      tasks.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
//      applications.add(tasks);
//      
//      GeoprismApplication settings = new GeoprismApplication();
//      settings.setId("settings");
//      settings.setLabel(LocalizationFacadeDTO.getFromBundles(request, "settings.menu"));
//      settings.setSrc("net/geoprism/images/settings.svg");
//      settings.setUrl("cgr/manage#/admin/settings");
//      settings.setDescription(LocalizationFacadeDTO.getFromBundles(request, "settings.menu"));
//      settings.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
//      applications.add(settings);
//      
//      GeoprismApplication hierarchies = new GeoprismApplication();
//      hierarchies.setId("hierarchies");
//      hierarchies.setLabel(LocalizationFacadeDTO.getFromBundles(request, "hierarchies.landing"));
//      hierarchies.setSrc("net/geoprism/images/hierarchy-icon-modified.svg");
//      hierarchies.setUrl("cgr/manage#/registry/hierarchies");
//      hierarchies.setDescription(LocalizationFacadeDTO.getFromBundles(request, "hierarchies.landing.description"));
//      hierarchies.addRole(RoleConstants.ADIM_ROLE);
////      hierarchies.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
//      hierarchies.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
//      hierarchies.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
//      hierarchies.addRole(RegistryConstants.API_CONSUMER_ROLE);
//      applications.add(hierarchies);
//      
//      return applications;
//    }
    
    GeoprismApplication hierarchies = new GeoprismApplication();
    hierarchies.setId("hierarchies");
    hierarchies.setLabel(LocalizationFacadeDTO.getFromBundles(request, "hierarchies.landing"));
    hierarchies.setSrc("net/geoprism/images/hierarchy-icon-modified.svg");
    hierarchies.setUrl("cgr/manage#/registry/hierarchies");
    hierarchies.setDescription(LocalizationFacadeDTO.getFromBundles(request, "hierarchies.landing.description"));
    hierarchies.addRole(RoleConstants.ADIM_ROLE);
    hierarchies.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    hierarchies.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    hierarchies.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    hierarchies.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    hierarchies.addRole(RegistryConstants.API_CONSUMER_ROLE);
    applications.add(hierarchies);
    
    GeoprismApplication masterLists = new GeoprismApplication();
    masterLists.setId("lists");
    masterLists.setLabel(LocalizationFacadeDTO.getFromBundles(request, "masterlists.landing"));
    masterLists.setSrc("net/geoprism/images/masterlist-icon-modified.svg");
    masterLists.setUrl("cgr/manage#/registry/master-lists");
    masterLists.setDescription(LocalizationFacadeDTO.getFromBundles(request, "masterlists.landing.description"));
    masterLists.addRole(RoleConstants.ADIM_ROLE);
    masterLists.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    masterLists.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    masterLists.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    masterLists.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    masterLists.addRole(RegistryConstants.API_CONSUMER_ROLE);
    applications.add(masterLists);
    
    GeoprismApplication uploads = new GeoprismApplication();
    uploads.setId("uploads");
    uploads.setLabel(LocalizationFacadeDTO.getFromBundles(request, "uploads.landing"));
    uploads.setSrc("net/geoprism/images/dm_icon.svg");
    uploads.setUrl("cgr/manage#/registry/data");
    uploads.setDescription(LocalizationFacadeDTO.getFromBundles(request, "uploads.landing.description"));
    uploads.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    uploads.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(uploads);
    
    GeoprismApplication scheduledJobs = new GeoprismApplication();
    scheduledJobs.setId("scheduledJobs");
    scheduledJobs.setLabel(new String(LocalizationFacadeDTO.getFromBundles(request, "scheduledjobs.menu")));
    scheduledJobs.setSrc("net/geoprism/images/job-scheduler.svg");
    scheduledJobs.setUrl("cgr/manage#/registry/scheduled-jobs");
    scheduledJobs.setDescription(LocalizationFacadeDTO.getFromBundles(request, "scheduledjobs.menu"));
    scheduledJobs.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    scheduledJobs.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(scheduledJobs);
    
    GeoprismApplication management = new GeoprismApplication();
    management.setId("locations");
    management.setLabel(LocalizationFacadeDTO.getFromBundles(request, "navigator.landing"));
    management.setSrc("net/geoprism/images/map_icon.svg");
    management.setUrl("cgr/manage#/registry/location-manager");
    management.setDescription(LocalizationFacadeDTO.getFromBundles(request, "navigator.landing.description"));
    management.addRole(RoleConstants.ADIM_ROLE);
    management.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    management.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    management.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    management.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    applications.add(management);
    
    GeoprismApplication requests = new GeoprismApplication();
    requests.setId("requests");
    requests.setLabel(LocalizationFacadeDTO.getFromBundles(request, "requests.landing"));
    requests.setSrc("net/geoprism/images/update-icon-modified.svg");
    requests.setUrl("cgr/manage#/registry/change-requests");
    requests.setDescription(LocalizationFacadeDTO.getFromBundles(request, "requests.landing.description"));
    requests.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    requests.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    requests.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(requests);
    
    GeoprismApplication tasks = new GeoprismApplication();
    tasks.setId("tasks");
    tasks.setLabel(LocalizationFacadeDTO.getFromBundles(request, "header.tasks"));
    tasks.setSrc("net/geoprism/images/task.svg");
    tasks.setUrl("cgr/manage#/registry/tasks");
    tasks.setDescription(LocalizationFacadeDTO.getFromBundles(request, "header.tasks"));
    tasks.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    tasks.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(tasks);
    
    GeoprismApplication settings = new GeoprismApplication();
    settings.setId("settings");
    settings.setLabel(LocalizationFacadeDTO.getFromBundles(request, "settings.menu"));
    settings.setSrc("net/geoprism/images/settings.svg");
    settings.setUrl("cgr/manage#/admin/settings");
    settings.setDescription(LocalizationFacadeDTO.getFromBundles(request, "settings.menu"));
    settings.addRole(RoleConstants.ADIM_ROLE);
    settings.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    settings.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    settings.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    settings.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    applications.add(settings);
    

    return applications;
  }

  /*
   * Expose public endpoints to allow non-logged in users to hit controller
   * endpoints
   */
  @Override
  public Set<String> getPublicEndpoints()
  {
    Set<String> endpoints = super.getPublicEndpoints();
    endpoints.add("cgr/manage");
    endpoints.add("registryaccount/inviteComplete");
    endpoints.add("registryaccount/newUserInstance");
    return endpoints;
  }

  @Override
  public String getHomeUrl()
  {
    return "/cgr/manage";
  }

  @Override
  public String getLoginUrl()
  {
    return "/cgr/manage#login";
  }
  
  @Override
  public String getServerVersion()
  {
    return RegistryVersionProperties.getInstance().getVersion();
  }

}
