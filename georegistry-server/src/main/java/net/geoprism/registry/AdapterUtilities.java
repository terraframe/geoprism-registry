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
  public List<GeoObjectType> getAncestors(ServerGeoObjectType child, String code)
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

  public JsonArray getHierarchiesForType(ServerGeoObjectType type, Boolean includeTypes)
  {
    HierarchyType[] hierarchyTypes = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    JsonArray hierarchies = new JsonArray();
    Universal root = Universal.getRoot();

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      ServerHierarchyType sType = ServerHierarchyType.get(hierarchyType);

      // Note: Ordered ancestors always includes self
      Collection<?> parents = GeoEntityUtil.getOrderedAncestors(root, type.getUniversal(), sType.getUniversalType());

      if (parents.size() > 1)
      {
        JsonObject object = new JsonObject();
        object.addProperty("code", hierarchyType.getCode());
        object.addProperty("label", hierarchyType.getLabel().getValue());

        if (includeTypes)
        {
          JsonArray pArray = new JsonArray();

          for (Object parent : parents)
          {
            ServerGeoObjectType pType = ServerGeoObjectType.get((Universal) parent);

            if (!pType.getCode().equals(type.getCode()))
            {
              JsonObject pObject = new JsonObject();
              pObject.addProperty("code", pType.getCode());
              pObject.addProperty("label", pType.getLabel().getValue());

              pArray.add(pObject);
            }
          }

          object.add("parents", pArray);
        }

        hierarchies.add(object);
      }
    }

    if (hierarchies.size() == 0)
    {
      /*
       * This is a root type so include all hierarchies
       */

      for (HierarchyType hierarchyType : hierarchyTypes)
      {
        JsonObject object = new JsonObject();
        object.addProperty("code", hierarchyType.getCode());
        object.addProperty("label", hierarchyType.getLabel().getValue());
        object.add("parents", new JsonArray());

        hierarchies.add(object);
      }
    }

    return hierarchies;
  }
}
