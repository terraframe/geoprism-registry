package net.geoprism.registry.model;

import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.system.gis.geo.Universal;

public class RootGeoObjectType extends ServerGeoObjectType
{
  public static final RootGeoObjectType INSTANCE = new RootGeoObjectType(Universal.getRoot());

  public RootGeoObjectType(Universal universal)
  {
    super(null, universal, null, null);
  }

  @Override
  public boolean getIsAbstract()
  {
    return false;
  }

  @Override
  public String getCode()
  {
    return Term.ROOT_KEY;
  }
}
