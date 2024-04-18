/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.request;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.GraphRepoServiceIF;
import net.geoprism.registry.service.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;
import net.geoprism.registry.xml.XMLExporter;

@Service
@Primary
public class GPRGeoObjectTypeService extends GeoObjectTypeService implements GeoObjectTypeServiceIF
{
  @Autowired
  private GeoObjectTypePermissionServiceIF typePermissions;

  @Autowired
  private RolePermissionService            rolePermissions;

  @Autowired
  private GraphRepoServiceIF               service;

  @Request(RequestType.SESSION)
  public void importTypes(String sessionId, String json, InputStream istream)
  {
    JsonArray orgCodes = JsonParser.parseString(json).getAsJsonArray();

    for (int i = 0; i < orgCodes.size(); i++)
    {
      String orgCode = orgCodes.get(i).getAsString();

      ServerOrganization org = ServerOrganization.getByCode(orgCode);

      if (!org.getEnabled())
      {
        throw new UnsupportedOperationException();
      }

      this.typePermissions.enforceCanCreate(orgCode, true);
    }

    GeoRegistryUtil.importTypes(json, istream);

    this.service.refreshMetadataCache();

    SerializedListTypeCache.getInstance().clear();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));
  }

  @Request(RequestType.SESSION)
  public InputStream exportTypes(String sessionId, String code)
  {
    this.rolePermissions.enforceRA(code);

    ServerOrganization organization = ServerOrganization.getByCode(code);

    if (!organization.getEnabled())
    {
      throw new UnsupportedOperationException();
    }

    XMLExporter exporter = new XMLExporter(organization);
    exporter.build();

    return exporter.write();
  }

  @Override
  @Request(RequestType.SESSION)
  public GeoObjectType updateGeoObjectType(String sessionId, String gtJSON)
  {
    GeoObjectType got = super.updateGeoObjectType(sessionId, gtJSON);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));

    return got;
  }

  @Override
  @Request(RequestType.SESSION)
  public GeoObjectType createGeoObjectType(String sessionId, String gtJSON)
  {
    GeoObjectType got = super.createGeoObjectType(sessionId, gtJSON);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));

    return got;
  }
}
