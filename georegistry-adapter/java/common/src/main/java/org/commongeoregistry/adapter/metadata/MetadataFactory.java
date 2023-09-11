/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class MetadataFactory
{
  public static Term newTerm(String code, LocalizedValue label, LocalizedValue description, RegistryAdapter registry)
  {
    Term t = new Term(code, label, description);

    registry.getMetadataCache().addTerm(t);

    return t;
  }
  
  public static OrganizationDTO newOrganization(String code, LocalizedValue label, LocalizedValue contactInfo, RegistryAdapter registry)
  {
    OrganizationDTO organization = new OrganizationDTO(code, label, contactInfo);

    registry.getMetadataCache().addOrganization(organization);

    return organization;
  }

  public static HierarchyType newHierarchyType(String code, LocalizedValue label, LocalizedValue description, String organizationCode, RegistryAdapter registry)
  {
    HierarchyType ht = new HierarchyType(code, label, description, organizationCode);

    registry.getMetadataCache().addHierarchyType(ht);

    return ht;
  }

  public static GeoObjectType newGeoObjectType(String code, GeometryType geometryType, LocalizedValue label, LocalizedValue description, Boolean isGeometryEditable, String organizationCode, RegistryAdapter registry)
  {
    GeoObjectType got = new GeoObjectType(code, geometryType, label, description, isGeometryEditable, organizationCode, registry);

    registry.getMetadataCache().addGeoObjectType(got);

    return got;
  }
}
