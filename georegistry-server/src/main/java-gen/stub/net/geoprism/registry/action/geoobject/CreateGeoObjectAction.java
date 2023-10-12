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
package net.geoprism.registry.action.geoobject;

import java.util.Arrays;
import java.util.Set;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.geoobject.CreateGeoObjectActionDTO;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Session;

import net.geoprism.graphrepo.permission.GeoObjectPermissionServiceIF;
import net.geoprism.registry.action.ActionJsonAdapters;
import net.geoprism.registry.action.ChangeRequestPermissionService;
import net.geoprism.registry.action.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.GPRGeoObjectPermissionService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class CreateGeoObjectAction extends CreateGeoObjectActionBase
{
  private static final long   serialVersionUID = 154658500;

  private static final Logger logger           = LoggerFactory.getLogger(CreateGeoObjectAction.class);

  public CreateGeoObjectAction()
  {
    super();
  }

  @Override
  public void execute()
  {
    GeoObjectBusinessServiceIF objectService = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    String sJson = this.getGeoObjectJson();

    GeoObjectOverTime geoObject = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), sJson);

    objectService.apply(geoObject, true, false);

    ServerGeoObjectIF child = objectService.getGeoObjectByCode(geoObject.getCode(), geoObject.getType().getCode());

    ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(child.getType(), this.getParentJson());

    objectService.setParents(child, ptnOt);
  }

  @Override
  public void apply()
  {
    String sJson = this.getGeoObjectJson();

    GeoObjectOverTime geoObject = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), sJson);

    ServerGeoObjectType type = ServerGeoObjectType.get(geoObject.getType());

    GeoObjectPermissionServiceIF geoObjectPermissionService = new GPRGeoObjectPermissionService();
    geoObjectPermissionService.enforceCanCreateCR(type.getOrganization().getCode(), type);

    super.apply();
  }

  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);

    CreateGeoObjectActionDTO castedDTO = (CreateGeoObjectActionDTO) dto;

    this.setGeoObjectJson(castedDTO.getGeoObject().toString());
  }

  @Override
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(CreateGeoObjectAction.class, new ActionJsonAdapters.CreateGeoObjectActionSerializer());

    return (JsonObject) builder.create().toJsonTree(this);
  }

  @Override
  public void buildFromJson(JSONObject joAction)
  {
    super.buildFromJson(joAction);

    Set<ChangeRequestPermissionAction> perms = new ChangeRequestPermissionService().getPermissions(this.getAllRequest().next());

    if (perms.containsAll(Arrays.asList(ChangeRequestPermissionAction.WRITE_DETAILS)))
    {
      this.setGeoObjectJson(joAction.getJSONObject(CreateGeoObjectAction.GEOOBJECTJSON).toString());
    }
  }

  @Override
  protected String getMessage()
  {
    GeoObjectOverTime go = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), this.getGeoObjectJson());
    GeoObjectType got = go.getType();

    String message = LocalizationFacade.localize("change.request.email.create.object");
    message = message.replaceAll("\\{0\\}", go.getCode());
    message = message.replaceAll("\\{1\\}", got.getLabel().getValue(Session.getCurrentLocale()));

    return message;
  }

}
