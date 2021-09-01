/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.model.ServerHierarchyType;

public class HierarchyMetadata extends HierarchyMetadataBase
{
  private static final long serialVersionUID = -1833634695;
  
  public static final String TYPE_LABEL = "hierarchyType.label";

  public HierarchyMetadata()
  {
    super();
  }

  @Override
  protected String buildKey()
  {
    return this.getMdTermRelationshipOid();
  }

  public static ServerHierarchyType getHierarchyType(String key)
  {
    HierarchyMetadata hierarchy = HierarchyMetadata.getByKey(key);
    MdTermRelationship mdTermRelationship = hierarchy.getMdTermRelationship();

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(mdTermRelationship);
    return hierarchyType;
  }

  public static void deleteByRelationship(MdTermRelationship mdRelationship)
  {
    HierarchyMetadataQuery query = new HierarchyMetadataQuery(new QueryFactory());
    query.WHERE(query.getMdTermRelationship().EQ(mdRelationship));

    List<? extends HierarchyMetadata> hierarchies = query.getIterator().getAll();

    for (HierarchyMetadata hierarchy : hierarchies)
    {
      hierarchy.delete();
    }
  }
  
  public String getClassDisplayLabel()
  {
    return sGetClassDisplayLabel();
  }
  
  public static String sGetClassDisplayLabel()
  {
    return LocalizationFacade.localize(TYPE_LABEL);
  }
  
  public static String getAttributeDisplayLabel(String attributeName)
  {
    return LocalizationFacade.localize("hierarchyType.attr."  + attributeName);
  }

}
