package net.geoprism.registry.etl.fhir;

import net.geoprism.account.OauthServer;
import net.geoprism.registry.graph.FhirExternalSystem;

public class FhirConnectionFactory
{
  public static FhirConnection get(FhirExternalSystem system)
  {
    OauthServer oauth = system.getOauthServer();

    if (oauth != null)
    {
      return new OauthFhirConnection(system, oauth);
    }

    return new BasicFhirConnection(system);
  }
}
