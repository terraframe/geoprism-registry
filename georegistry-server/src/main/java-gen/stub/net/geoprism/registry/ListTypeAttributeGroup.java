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
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdAttribute;

public class ListTypeAttributeGroup extends ListTypeAttributeGroupBase
{
  private static final long serialVersionUID = 862878064;
  
  public ListTypeAttributeGroup()
  {
    super();
  }
  
  @Override
  protected String buildKey()
  {
    if (this.getTargetAttributeOid() != null && this.getTargetAttributeOid().length() > 0)
    {
      return this.getTargetAttributeOid();
    }

    return super.buildKey();
  }

  public static void create(ListTypeVersion version, MdAttribute source, MdAttribute target)
  {
    ListTypeAttributeGroup group = new ListTypeAttributeGroup();
    group.setVersion(version);
    group.setSourceAttribute(source);
    group.setTargetAttribute(target);
    group.apply();
  }

  public static void deleteAll(ListTypeVersion version)
  {
    ListTypeAttributeGroupQuery query = new ListTypeAttributeGroupQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    OIterator<? extends ListTypeAttributeGroup> it = query.getIterator();

    try
    {
      List<? extends ListTypeAttributeGroup> groups = it.getAll();

      for (ListTypeAttributeGroup group : groups)
      {
        group.delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  public static void remove(MdAttributeConcreteDAOIF mdAttribute)
  {
    ListTypeAttributeGroupQuery query = new ListTypeAttributeGroupQuery(new QueryFactory());
    query.WHERE(query.getTargetAttribute().EQ(mdAttribute.getOid()));
    query.OR(query.getSourceAttribute().EQ(mdAttribute.getOid()));

    OIterator<? extends ListTypeAttributeGroup> it = query.getIterator();

    try
    {
      List<? extends ListTypeAttributeGroup> groups = it.getAll();

      for (ListTypeAttributeGroup group : groups)
      {
        group.delete();
      }
    }
    finally
    {
      it.close();
    }
  }


}
