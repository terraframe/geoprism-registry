/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
//      tasks.setSrc("assets/task.svg");
//      tasks.setUrl("cgr/manage#/registry/tasks");
//      tasks.setDescription(LocalizationFacadeDTO.getFromBundles(request, "header.tasks"));
//      tasks.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
//      applications.add(tasks);
//      
//      GeoprismApplication settings = new GeoprismApplication();
//      settings.setId("settings");
//      settings.setLabel(LocalizationFacadeDTO.getFromBundles(request, "settings.menu"));
//      settings.setSrc("assets/settings.svg");
//      settings.setUrl("cgr/manage#/admin/settings");
//      settings.setDescription(LocalizationFacadeDTO.getFromBundles(request, "settings.menu"));
//      settings.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
//      applications.add(settings);
//      
//      GeoprismApplication hierarchies = new GeoprismApplication();
//      hierarchies.setId("hierarchies");
//      hierarchies.setLabel(LocalizationFacadeDTO.getFromBundles(request, "hierarchies.landing"));
//      hierarchies.setSrc("assets/hierarchy-icon-modified.svg");
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
    hierarchies.setSrc("assets/hierarchy-icon-modified.svg");
    hierarchies.setUrl("#/registry/hierarchies");
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
    masterLists.setSrc("assets/masterlist-icon-modified.svg");
    masterLists.setUrl("#/registry/master-lists");
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
    uploads.setSrc("assets/dm_icon.svg");
    uploads.setUrl("#/registry/data");
    uploads.setDescription(LocalizationFacadeDTO.getFromBundles(request, "uploads.landing.description"));
    uploads.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    uploads.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    uploads.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(uploads);
    
    GeoprismApplication scheduledJobs = new GeoprismApplication();
    scheduledJobs.setId("scheduledJobs");
    scheduledJobs.setLabel(LocalizationFacadeDTO.getFromBundles(request, "scheduledjobs.menu"));
    scheduledJobs.setSrc("assets/job-scheduler.svg");
    scheduledJobs.setUrl("#/registry/scheduled-jobs");
    scheduledJobs.setDescription(LocalizationFacadeDTO.getFromBundles(request, "scheduledjobs.menu"));
    scheduledJobs.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    scheduledJobs.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    scheduledJobs.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(scheduledJobs);
    
    GeoprismApplication management = new GeoprismApplication();
    management.setId("locations");
    management.setLabel(LocalizationFacadeDTO.getFromBundles(request, "navigator.landing"));
    management.setSrc("assets/map_icon.svg");
    management.setUrl("#/registry/location-manager");
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
    requests.setSrc("assets/update-icon-modified.svg");
    requests.setUrl("#/registry/change-requests");
    requests.setDescription(LocalizationFacadeDTO.getFromBundles(request, "requests.landing.description"));
    requests.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    requests.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    requests.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    requests.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(requests);
    
    GeoprismApplication tasks = new GeoprismApplication();
    tasks.setId("tasks");
    tasks.setLabel(LocalizationFacadeDTO.getFromBundles(request, "header.tasks"));
    tasks.setSrc("assets/task.svg");
    tasks.setUrl("#/registry/tasks");
    tasks.setDescription(LocalizationFacadeDTO.getFromBundles(request, "header.tasks"));
    tasks.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    tasks.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    tasks.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(tasks);
    
    GeoprismApplication historicalEvents = new GeoprismApplication();
    historicalEvents.setId("historicalEvents");
    historicalEvents.setLabel(LocalizationFacadeDTO.getFromBundles(request, "historical.events"));
    historicalEvents.setSrc("assets/historical-events.svg");
    historicalEvents.setUrl("#/registry/historical-events");
    historicalEvents.setDescription(LocalizationFacadeDTO.getFromBundles(request, "historical.events.description"));
    historicalEvents.addRole(RoleConstants.ADIM_ROLE);
    historicalEvents.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    historicalEvents.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    historicalEvents.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    historicalEvents.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    applications.add(historicalEvents);
    

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
    endpoints.add("api/cgrsession/ologin");
    endpoints.add("api/cgrsession/login");
    endpoints.add("api/cgr/localization-map");
    endpoints.add("api/cgr/current-locale");
    endpoints.add("api/cgr/locales");
    endpoints.add("api/cgr/configuration");
    endpoints.add("api/asset/view");
    endpoints.add("api/oauth/get-public");
    endpoints.add("api/registryaccount/inviteComplete");
    endpoints.add("api/registryaccount/newUserInstance");
    endpoints.add("api/master-list/tile");    
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
