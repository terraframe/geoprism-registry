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
package com.runwaysdk.build.domain;

import java.util.Set;
import java.util.stream.Collectors;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.HierarchicalRelationshipType;
import net.geoprism.registry.InheritedHierarchyAnnotation;
import net.geoprism.registry.InheritedHierarchyAnnotationQuery;
import net.geoprism.registry.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class PatchInheritedAnnotation
{
  public static void main(String[] args)
  {
    new PatchInheritedAnnotation().doIt();
  }

  @Request
  private void doIt()
  {
    this.transaction();
  }

  @Transaction
  private void transaction()
  {
    InheritedHierarchyAnnotationQuery query = new InheritedHierarchyAnnotationQuery(new QueryFactory());
    query.ORDER_BY(query.getForHierarchicalRelationshipType(), SortOrder.DESC);
    query.ORDER_BY(query.getCreateDate(), SortOrder.DESC);

    try (OIterator<? extends InheritedHierarchyAnnotation> iterator = query.getIterator())
    {
      InheritedHierarchyAnnotation prev = null;

      while (iterator.hasNext())
      {
        InheritedHierarchyAnnotation annotation = iterator.next();

        if (prev != null && prev.getForHierarchicalRelationshipTypeOid().equals(annotation.getForHierarchicalRelationshipTypeOid()))
        {
          annotation.delete();
        }
        else if (annotation.getForHierarchicalRelationshipTypeOid() == null || annotation.getForHierarchicalRelationshipTypeOid().length() == 0)
        {
          annotation.delete();
        }
        else
        {
          // Determine if the inherited hierarchy and for hierarchy have the
          // same root
          ServerGeoObjectType inheritedNode = ServerGeoObjectType.get(annotation.getUniversal());
          HierarchicalRelationshipType inheritedHierarchicalType = annotation.getInheritedHierarchicalRelationshipType();

          ServerHierarchyType inheritedHierarchy = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class).get(inheritedHierarchicalType);

          Set<String> rootCodes = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class).getRootGeoObjectTypes(inheritedHierarchy).stream().map(type -> type.getGeoObjectType().getCode()).collect(Collectors.toSet());

          if (rootCodes.contains(inheritedNode.getCode()))
          {
            annotation.delete();
          }
          else
          {
            prev = annotation;
          }
        }

      }
    }
  }

}
