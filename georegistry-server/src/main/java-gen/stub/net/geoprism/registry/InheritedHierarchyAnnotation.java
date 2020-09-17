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

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;

public class InheritedHierarchyAnnotation extends InheritedHierarchyAnnotationBase
{
  private static final long serialVersionUID = -918188612;

  public InheritedHierarchyAnnotation()
  {
    super();
  }

  public static List<? extends InheritedHierarchyAnnotation> getByUniversal(Universal universal)
  {
    InheritedHierarchyAnnotationQuery query = new InheritedHierarchyAnnotationQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));

    try (OIterator<? extends InheritedHierarchyAnnotation> iterator = query.getIterator())
    {
      return iterator.getAll();
    }
  }

  public static List<? extends InheritedHierarchyAnnotation> getByRelationship(MdTermRelationship mdRelationship)
  {
    InheritedHierarchyAnnotationQuery query = new InheritedHierarchyAnnotationQuery(new QueryFactory());
    query.WHERE(query.getInheritedHierarchy().EQ(mdRelationship));
    query.OR(query.getForHierarchy().EQ(mdRelationship));

    try (OIterator<? extends InheritedHierarchyAnnotation> iterator = query.getIterator())
    {
      return iterator.getAll();
    }
  }

  public static InheritedHierarchyAnnotation get(Universal universal, MdTermRelationship forRelationship)
  {
    InheritedHierarchyAnnotationQuery query = new InheritedHierarchyAnnotationQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));
    query.AND(query.getForHierarchy().EQ(forRelationship));

    try (OIterator<? extends InheritedHierarchyAnnotation> iterator = query.getIterator())
    {
      List<? extends InheritedHierarchyAnnotation> list = iterator.getAll();

      if (list.size() > 0)
      {
        return list.get(0);
      }

      return null;
    }
  }
}
