/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
