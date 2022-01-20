/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import java.util.List;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.model.ServerHierarchyType;

public class AttributeHierarchy extends AttributeHierarchyBase
{
  private static final long serialVersionUID = -1818416302;

  public AttributeHierarchy()
  {
    super();
  }

  public static ServerHierarchyType getHierarchyType(String key)
  {
    AttributeHierarchy hierarchy = AttributeHierarchy.getByKey(key);
    MdTermRelationship mdTermRelationship = hierarchy.getMdTermRelationship();

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(mdTermRelationship);
    return hierarchyType;
  }

  public static void deleteByUniversal(Universal uni)
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(uni.getMdBusinessOid());

    deleteByMdBusiness(mdBusiness);
  }

  public static void deleteByMdBusiness(MdBusinessDAOIF mdBusiness)
  {
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributes();

    for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
    {
      if (mdAttribute instanceof MdAttributeReferenceDAOIF)
      {
        MdBusinessDAOIF referencedMdBusiness = ( (MdAttributeReferenceDAOIF) mdAttribute ).getReferenceMdBusinessDAO();

        if (referencedMdBusiness.definesType().equals(GeoEntity.CLASS))
        {
          AttributeHierarchyQuery query = new AttributeHierarchyQuery(new QueryFactory());
          query.WHERE(query.getMdAttribute().EQ(mdAttribute.getOid()));

          List<? extends AttributeHierarchy> hierarchies = query.getIterator().getAll();

          for (AttributeHierarchy hierarchy : hierarchies)
          {
            hierarchy.delete();
          }
        }
      }
    }
  }

  public static void deleteByRelationship(MdTermRelationship mdRelationship)
  {
    AttributeHierarchyQuery query = new AttributeHierarchyQuery(new QueryFactory());
    query.WHERE(query.getMdTermRelationship().EQ(mdRelationship));

    List<? extends AttributeHierarchy> hierarchies = query.getIterator().getAll();

    for (AttributeHierarchy hierarchy : hierarchies)
    {
      hierarchy.delete();
    }
  }
}
