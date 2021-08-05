package net.geoprism.registry.etl.fhir;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;

public class Facility
{
  private Organization organization;

  private Location     location;

  public Facility(Organization organization, Location location)
  {
    super();
    this.organization = organization;
    this.location = location;
  }

  public Organization getOrganization()
  {
    return organization;
  }

  public Location getLocation()
  {
    return location;
  }
}
