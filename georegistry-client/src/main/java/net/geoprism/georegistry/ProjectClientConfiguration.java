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

public class ProjectClientConfiguration extends DefaultClientConfiguration implements ClientConfigurationIF
{

  @Override
  public List<GeoprismApplication> getApplications(ClientRequestIF request)
  {
    List<GeoprismApplication> applications = new LinkedList<GeoprismApplication>();
    
    return applications;
  }

  /*
   * Expose public endpoints to allow non-logged in users to hit controller endpoints
   */
  @Override
  public Set<String> getPublicEndpoints()
  {
    Set<String> endpoints = new TreeSet<String>();

    return endpoints;
  }

}
