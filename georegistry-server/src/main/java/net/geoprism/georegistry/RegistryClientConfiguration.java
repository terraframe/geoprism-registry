package net.geoprism.georegistry;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.geoprism.ClientConfigurationIF;
import net.geoprism.DefaultClientConfiguration;
import net.geoprism.GeoprismApplication;
import net.geoprism.localization.LocalizationFacadeDTO;

import com.runwaysdk.constants.ClientRequestIF;

public class RegistryClientConfiguration extends DefaultClientConfiguration implements ClientConfigurationIF
{

  @Override
  public List<GeoprismApplication> getApplications(ClientRequestIF request)
  {
    GeoprismApplication management = new GeoprismApplication();
    management.setId("projects");
    management.setLabel(LocalizationFacadeDTO.getFromBundles(request, "projects.landing"));
    management.setSrc("net/geoprism/images/dm_icon.svg");
    management.setUrl("project/management");

    List<GeoprismApplication> applications = new LinkedList<GeoprismApplication>();
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
