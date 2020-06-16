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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class AdapterUtilities
{
  public synchronized static AdapterUtilities getInstance()
  {
    return ServiceFactory.getUtilities();
  }

  public AdapterUtilities()
  {
  }

  /**
   * Returns all ancestors of a GeoObjectType
   * 
   * @param GeoObjectType
   *          child
   * @param code
   *          The Hierarchy code
   * @return
   */
  @Request
  public List<GeoObjectType> getTypeAncestors(ServerGeoObjectType child, String code)
  {
    List<GeoObjectType> ancestors = new LinkedList<GeoObjectType>();

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(code);

    Collection<com.runwaysdk.business.ontology.Term> list = GeoEntityUtil.getOrderedAncestors(Universal.getRoot(), child.getUniversal(), hierarchyType.getUniversalType());

    list.forEach(term -> {
      Universal parent = (Universal) term;

      if (!parent.getKeyName().equals(Universal.ROOT) && !parent.getOid().equals(child.getUniversal().getOid()))
      {
        ancestors.add(ServerGeoObjectType.get(parent).getType());
      }
    });

    return ancestors;
  }
  
  @Request
  public List<GeoObjectType> getTypeParents(ServerGeoObjectType child, String code)
  {
    List<GeoObjectType> ancestors = new LinkedList<GeoObjectType>();

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(code);

    Collection<com.runwaysdk.business.ontology.Term> list = GeoEntityUtil.getOrderedAncestors(Universal.getRoot(), child.getUniversal(), hierarchyType.getUniversalType());

    list.forEach(term -> {
      Universal parent = (Universal) term;

      if (!parent.getKeyName().equals(Universal.ROOT) && !parent.getOid().equals(child.getUniversal().getOid()))
      {
        ancestors.add(ServerGeoObjectType.get(parent).getType());
      }
    });

    return ancestors;
  }

  // public HierarchyType getHierarchyTypeById(String oid)
  // {
  // MdTermRelationship mdTermRel = MdTermRelationship.get(oid);
  //
  // HierarchyType ht =
  // ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRel);
  //
  // return ht;
  // }

  // public GeoObjectType getGeoObjectTypeById(String id)
  // {
  // Universal uni = Universal.get(id);
  //
  // return
  // ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(uni.getKey()).get();
  // }
}
