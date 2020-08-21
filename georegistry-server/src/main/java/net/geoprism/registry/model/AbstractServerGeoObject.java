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
package net.geoprism.registry.model;

import java.util.Collection;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;

public abstract class AbstractServerGeoObject implements ServerGeoObjectIF
{

  public JsonArray getHierarchiesForGeoObject()
  {
    ServerGeoObjectType geoObjectType = this.getType();

    List<HierarchyType> hierarchyTypes = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    JsonArray hierarchies = new JsonArray();
    Universal root = Universal.getRoot();

    SingleActorDAOIF actor = null;
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      actor = Session.getCurrentSession().getUser();
    }

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      if (ServiceFactory.getHierarchyPermissionService().canRead(actor, hierarchyType.getOrganizationCode(), PermissionContext.WRITE))
      {
        ServerHierarchyType sType = ServerHierarchyType.get(hierarchyType);

        // Note: Ordered ancestors always includes self
        Collection<?> uniParents = GeoEntityUtil.getOrderedAncestors(root, geoObjectType.getUniversal(), sType.getUniversalType());

        ParentTreeNode ptnAncestors = this.getParentGeoObjects(null, true).toNode(true);

        if (uniParents.size() > 1)
        {
          JsonObject object = new JsonObject();
          object.addProperty("code", hierarchyType.getCode());
          object.addProperty("label", hierarchyType.getLabel().getValue());

          JsonArray pArray = new JsonArray();

          for (Object parent : uniParents)
          {
            ServerGeoObjectType pType = ServerGeoObjectType.get((Universal) parent);

            if (!pType.getCode().equals(geoObjectType.getCode()))
            {
              JsonObject pObject = new JsonObject();
              pObject.addProperty("code", pType.getCode());
              pObject.addProperty("label", pType.getLabel().getValue());

              List<ParentTreeNode> ptns = ptnAncestors.findParentOfType(pType.getCode());
              for (ParentTreeNode ptn : ptns)
              {
                if (ptn.getHierachyType().getCode().equals(hierarchyType.getCode()))
                {
                  pObject.add("ptn", ptn.toJSON());
                  break; // TODO Sibling ancestors
                }
              }

              pArray.add(pObject);
            }
          }

          object.add("parents", pArray);

          hierarchies.add(object);
        }
      }
    }

    if (hierarchies.size() == 0)
    {
      /*
       * This is a root type so include all hierarchies
       */

      for (HierarchyType hierarchyType : hierarchyTypes)
      {
        if (ServiceFactory.getHierarchyPermissionService().canRead(actor, hierarchyType.getOrganizationCode(), PermissionContext.WRITE))
        {
          JsonObject object = new JsonObject();
          object.addProperty("code", hierarchyType.getCode());
          object.addProperty("label", hierarchyType.getLabel().getValue());
          object.add("parents", new JsonArray());

          hierarchies.add(object);
        }
      }
    }

    return hierarchies;
  }
}
