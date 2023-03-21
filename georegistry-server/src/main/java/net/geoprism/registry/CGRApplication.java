package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.rbac.RoleConstants;

public class CGRApplication
{
  private String      oid;

  private String      src;

  private String      label;
  
  private String      description;

  private String      url;
  
  private String      customHomeUrl;

  private Set<String> roleNames;

  public CGRApplication()
  {
    this.roleNames = new TreeSet<String>();
  }

  public String getOid()
  {
    return oid;
  }

  public void setId(String oid)
  {
    this.oid = oid;
  }

  public String getSrc()
  {
    return src;
  }

  public void setSrc(String src)
  {
    this.src = src;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }
  
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public void addRole(String roleName)
  {
    this.roleNames.add(roleName);
  }

  public Set<String> getRoleNames()
  {
    return roleNames;
  }

  public void setRoleNames(Set<String> roleNames)
  {
    this.roleNames = roleNames;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public boolean isValid(Set<String> roleNames)
  {
    TreeSet<String> contains = new TreeSet<String>(this.roleNames);
    contains.retainAll(roleNames);

    return ( this.roleNames.size() == 0 || !contains.isEmpty() );
  }

  public JSONObject toJSON() throws JSONException
  {
    JSONObject object = new JSONObject();
    object.put("oid", this.oid);
    object.put("src", this.src);
    object.put("label", this.label);
    object.put("url", this.url);
    object.put("description", this.description);

    return object;
  }
  
  public static List<CGRApplication> getApplications(ClientRequestIF request)
  {
    List<CGRApplication> applications = new LinkedList<CGRApplication>();
    
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
//      CGRApplication tasks = new CGRApplication();
//      tasks.setId("tasks");
//      tasks.setLabel(LocalizationFacade.localize("header.tasks"));
//      tasks.setSrc("assets/task.svg");
//      tasks.setUrl("cgr/manage#/registry/tasks");
//      tasks.setDescription(LocalizationFacade.localize("header.tasks"));
//      tasks.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
//      applications.add(tasks);
//      
//      CGRApplication settings = new CGRApplication();
//      settings.setId("settings");
//      settings.setLabel(LocalizationFacade.localize("settings.menu"));
//      settings.setSrc("assets/settings.svg");
//      settings.setUrl("cgr/manage#/admin/settings");
//      settings.setDescription(LocalizationFacade.localize("settings.menu"));
//      settings.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
//      applications.add(settings);
//      
//      CGRApplication hierarchies = new CGRApplication();
//      hierarchies.setId("hierarchies");
//      hierarchies.setLabel(LocalizationFacade.localize("hierarchies.landing"));
//      hierarchies.setSrc("assets/hierarchy-icon-modified.svg");
//      hierarchies.setUrl("cgr/manage#/registry/hierarchies");
//      hierarchies.setDescription(LocalizationFacade.localize("hierarchies.landing.description"));
//      hierarchies.addRole(RoleConstants.ADIM_ROLE);
////      hierarchies.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
//      hierarchies.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
//      hierarchies.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
//      hierarchies.addRole(RegistryConstants.API_CONSUMER_ROLE);
//      applications.add(hierarchies);
//      
//      return applications;
//    }
    
    CGRApplication hierarchies = new CGRApplication();
    hierarchies.setId("hierarchies");
    hierarchies.setLabel(LocalizationFacade.localize("hierarchies.landing"));
    hierarchies.setSrc("assets/hierarchy-icon-modified.svg");
    hierarchies.setUrl("#/registry/hierarchies");
    hierarchies.setDescription(LocalizationFacade.localize("hierarchies.landing.description"));
    hierarchies.addRole(RoleConstants.ADMIN);
    hierarchies.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    hierarchies.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    hierarchies.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    hierarchies.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    hierarchies.addRole(RegistryConstants.API_CONSUMER_ROLE);
    applications.add(hierarchies);
    
    CGRApplication masterLists = new CGRApplication();
    masterLists.setId("lists");
    masterLists.setLabel(LocalizationFacade.localize("masterlists.landing"));
    masterLists.setSrc("assets/masterlist-icon-modified.svg");
    masterLists.setUrl("#/registry/master-lists");
    masterLists.setDescription(LocalizationFacade.localize("masterlists.landing.description"));
    masterLists.addRole(RoleConstants.ADMIN);
    masterLists.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    masterLists.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    masterLists.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    masterLists.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    masterLists.addRole(RegistryConstants.API_CONSUMER_ROLE);
    applications.add(masterLists);
    
    CGRApplication uploads = new CGRApplication();
    uploads.setId("uploads");
    uploads.setLabel(LocalizationFacade.localize("uploads.landing"));
    uploads.setSrc("assets/dm_icon.svg");
    uploads.setUrl("#/registry/data");
    uploads.setDescription(LocalizationFacade.localize("uploads.landing.description"));
    uploads.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    uploads.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    uploads.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(uploads);
    
    CGRApplication scheduledJobs = new CGRApplication();
    scheduledJobs.setId("scheduledJobs");
    scheduledJobs.setLabel(LocalizationFacade.localize("scheduledjobs.menu"));
    scheduledJobs.setSrc("assets/job-scheduler.svg");
    scheduledJobs.setUrl("#/registry/scheduled-jobs");
    scheduledJobs.setDescription(LocalizationFacade.localize("scheduledjobs.menu"));
    scheduledJobs.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    scheduledJobs.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    scheduledJobs.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(scheduledJobs);
    
    CGRApplication management = new CGRApplication();
    management.setId("locations");
    management.setLabel(LocalizationFacade.localize("navigator.landing"));
    management.setSrc("assets/map_icon.svg");
    management.setUrl("#/registry/location-manager");
    management.setDescription(LocalizationFacade.localize("navigator.landing.description"));
    management.addRole(RoleConstants.ADMIN);
    management.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    management.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    management.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    management.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    applications.add(management);
    
    CGRApplication requests = new CGRApplication();
    requests.setId("requests");
    requests.setLabel(LocalizationFacade.localize("requests.landing"));
    requests.setSrc("assets/update-icon-modified.svg");
    requests.setUrl("#/registry/change-requests");
    requests.setDescription(LocalizationFacade.localize("requests.landing.description"));
    requests.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    requests.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    requests.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    requests.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(requests);
    
    CGRApplication tasks = new CGRApplication();
    tasks.setId("tasks");
    tasks.setLabel(LocalizationFacade.localize("header.tasks"));
    tasks.setSrc("assets/task.svg");
    tasks.setUrl("#/registry/tasks");
    tasks.setDescription(LocalizationFacade.localize("header.tasks"));
    tasks.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    tasks.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    tasks.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    applications.add(tasks);
    
    CGRApplication historicalEvents = new CGRApplication();
    historicalEvents.setId("historicalEvents");
    historicalEvents.setLabel(LocalizationFacade.localize("historical.events"));
    historicalEvents.setSrc("assets/historical-events.svg");
    historicalEvents.setUrl("#/registry/historical-events");
    historicalEvents.setDescription(LocalizationFacade.localize("historical.events.description"));
    historicalEvents.addRole(RoleConstants.ADMIN);
    historicalEvents.addRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    historicalEvents.addRole(RegistryConstants.REGISTRY_ADMIN_ROLE);
    historicalEvents.addRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE);
    historicalEvents.addRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);
    applications.add(historicalEvents);
    

    return applications;
  }
}
