package net.geoprism.georegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.runwaysdk.constants.ClientRequestIF;

import net.geoprism.ClientConfigurationIF;
import net.geoprism.DefaultClientConfiguration;
import net.geoprism.GeoprismApplication;
import net.geoprism.GeoprismPatcher;
import net.geoprism.RoleConstants;
import net.geoprism.localization.LocalizationFacadeDTO;

public class RegistryClientConfiguration extends DefaultClientConfiguration implements ClientConfigurationIF
{
  
  public GeoprismPatcher getPatcher()
  {
    return new GeoregistryPatcher();
  }

  @Override
  public List<GeoprismApplication> getApplications(ClientRequestIF request)
  {
    GeoprismApplication hierarchies = new GeoprismApplication();
    hierarchies.setId("hierarchies");
    hierarchies.setLabel(LocalizationFacadeDTO.getFromBundles(request, "hierarchies.landing"));
    hierarchies.setSrc("net/geoprism/images/dm_icon.svg");
    hierarchies.setUrl("cgr/manage#/hierarchies");
    hierarchies.addRole(RoleConstants.ADIM_ROLE);
    hierarchies.addRole(RoleConstants.BUILDER_ROLE);
    
//    GeoprismApplication management = new GeoprismApplication();
//    management.setId("management");
//    management.setLabel(LocalizationFacadeDTO.getFromBundles(request, "geoprismLanding.dataManagement"));
//    management.setSrc("net/geoprism/images/dm_icon.svg");
//    management.setUrl("prism/home#data");
//    management.addRole(RoleConstants.ADIM_ROLE);
//    management.addRole(RoleConstants.BUILDER_ROLE);
    
    GeoprismApplication management = new GeoprismApplication();
    management.setId("locations");
    management.setLabel(LocalizationFacadeDTO.getFromBundles(request, "geoprismLanding.locationManagement"));
    management.setSrc("net/geoprism/images/dm_icon.svg");
    management.setUrl("nav/management#locations");
    management.addRole(RoleConstants.ADIM_ROLE);
    management.addRole(RoleConstants.BUILDER_ROLE);
    

    List<GeoprismApplication> applications = new LinkedList<GeoprismApplication>();
    applications.add(hierarchies);
    applications.add(management);

    return applications;
  }

  /*
   * Expose public endpoints to allow non-logged in users to hit controller
   * endpoints
   */
  @Override
  public Set<String> getPublicEndpoints()
  {
    return super.getPublicEndpoints();
  }

}
