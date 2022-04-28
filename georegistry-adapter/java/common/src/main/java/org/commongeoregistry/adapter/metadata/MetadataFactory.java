/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
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
