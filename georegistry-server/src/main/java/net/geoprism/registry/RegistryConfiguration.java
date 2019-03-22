package net.geoprism.registry;

import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.dataaccess.MdRelationshipDAOIF;
import com.runwaysdk.dataaccess.metadata.MdRelationshipDAO;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.ConfigurationIF;
import net.geoprism.DefaultConfiguration;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.ServiceFactory;

public class RegistryConfiguration extends DefaultConfiguration implements ConfigurationIF
{
  @Override
  public String getGeoEntityRelationship(MdRelationshipDAOIF mdRelationshipDAOIF)
  {
    if (mdRelationshipDAOIF.definesType().equals(AllowedIn.CLASS))
    {
      return MdRelationshipDAO.getMdRelationshipDAO(LocatedIn.CLASS).getOid();
    }

    try
    {
      ConversionService service = ServiceFactory.getConversionService();
      HierarchyType hierarchy = service.mdTermRelationshipToHierarchyType(MdTermRelationship.get(mdRelationshipDAOIF.getOid()));
      MdTermRelationship geoEntityRelationship = service.existingHierarchyToGeoEntityMdTermRelationiship(hierarchy);

      return geoEntityRelationship.getOid();
    }
    catch (Exception e)
    {
      return null;
    }
  }

}
