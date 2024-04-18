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
package net.geoprism.registry.service.request;

import java.util.List;

import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.service.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.service.permission.RolePermissionService;

@Service
@Primary
public class GPRHierarchyTypeService extends HierarchyTypeService implements HierarchyTypeServiceIF
{
  @Autowired
  private RolePermissionService permissions;

  @Autowired
  private HierarchyTypePermissionServiceIF hierarchyPermissions;
  
  @Autowired
  private GeoObjectTypePermissionServiceIF typePermissions;
  
  @Request(RequestType.SESSION)
  public JsonArray getHierarchyGroupedTypes(String sessionId)
  {
    final boolean isSRA = permissions.isSRA();

    JsonArray allHiers = new JsonArray();

    List<ServerHierarchyType> shts = ServiceFactory.getMetadataCache().getAllHierarchyTypes();

    for (ServerHierarchyType sht : shts)
    {
      final String htOrgCode = sht.getOrganizationCode();

      if (hierarchyPermissions.canRead(htOrgCode) && ( isSRA || permissions.isRA(htOrgCode) || permissions.isRM(htOrgCode) ))
      {
        JsonObject hierView = new JsonObject();
        hierView.addProperty("code", sht.getCode());
        hierView.addProperty("label", sht.getLabel().getValue());
        hierView.addProperty("orgCode", sht.getOrganizationCode());

        JsonArray allHierTypes = new JsonArray();

        List<ServerGeoObjectType> types = service.getAllTypes(sht, false);

        for (ServerGeoObjectType type : types)
        {
          final String gotOrgCode = type.getOrganizationCode();

          if (typePermissions.canRead(gotOrgCode, type, type.getIsPrivate()) && ( isSRA || permissions.isRA(gotOrgCode) || permissions.isRM(gotOrgCode, type) ))
          {
            if (type.getIsAbstract())
            {
              JsonObject superView = new JsonObject();
              superView.addProperty("code", type.getCode());
              superView.addProperty("label", type.getLabel().getValue());
              superView.addProperty("orgCode", type.getOrganizationCode());
              superView.addProperty("isAbstract", true);

              List<ServerGeoObjectType> subtypes = gotServ.getSubtypes(type);

              for (ServerGeoObjectType subtype : subtypes)
              {
                JsonObject typeView = new JsonObject();
                typeView.addProperty("code", subtype.getCode());
                typeView.addProperty("label", subtype.getLabel().getValue());
                typeView.addProperty("orgCode", subtype.getOrganization().getCode());
                typeView.add("super", superView);

                allHierTypes.add(typeView);
              }
            }
            else
            {
              JsonObject typeView = new JsonObject();
              typeView.addProperty("code", type.getCode());
              typeView.addProperty("label", type.getLabel().getValue());
              typeView.addProperty("orgCode", type.getOrganizationCode());

              allHierTypes.add(typeView);
            }
          }
        }

        hierView.add("types", allHierTypes);

        allHiers.add(hierView);
      }
    }

    return allHiers;
  }
  
  @Override
  @Request(RequestType.SESSION)
  public HierarchyType createHierarchyType(String sessionId, String htJSON)
  {
    String code = GeoRegistryUtil.createHierarchyType(htJSON);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

//    return ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code).get();
    
    return service.toHierarchyType(ServerHierarchyType.get(code));
  }
}
