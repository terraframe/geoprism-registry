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

import java.util.List;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdAttribute;

public class MasterListAttributeGroup extends MasterListAttributeGroupBase
{
  private static final long serialVersionUID = 314069411;

  public MasterListAttributeGroup()
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

  public static void create(MasterList masterList, MdAttribute source, MdAttribute target)
  {
    MasterListAttributeGroup group = new MasterListAttributeGroup();
    group.setMasterList(masterList);
    group.setSourceAttribute(source);
    group.setTargetAttribute(target);
    group.apply();
  }

  public static void deleteAll(MasterList masterList)
  {
    MasterListAttributeGroupQuery query = new MasterListAttributeGroupQuery(new QueryFactory());
    query.WHERE(query.getMasterList().EQ(masterList));

    OIterator<? extends MasterListAttributeGroup> it = query.getIterator();

    try
    {
      List<? extends MasterListAttributeGroup> groups = it.getAll();

      for (MasterListAttributeGroup group : groups)
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
    MasterListAttributeGroupQuery query = new MasterListAttributeGroupQuery(new QueryFactory());
    query.WHERE(query.getTargetAttribute().EQ(mdAttribute.getOid()));
    query.OR(query.getSourceAttribute().EQ(mdAttribute.getOid()));

    OIterator<? extends MasterListAttributeGroup> it = query.getIterator();

    try
    {
      List<? extends MasterListAttributeGroup> groups = it.getAll();

      for (MasterListAttributeGroup group : groups)
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
