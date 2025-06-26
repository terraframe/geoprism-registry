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
package net.geoprism.registry.action.tree;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.tree.RemoveChildActionDTO;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Session;

import net.geoprism.registry.action.ActionJsonAdapters;
import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class RemoveChildAction extends RemoveChildActionBase
{
  private static final long serialVersionUID = -165581118;
  
  private static final Logger logger = LoggerFactory.getLogger(RemoveChildAction.class);
  
  @Override
  public void execute(ServerGeoObjectEventBuilder builder)
  {
    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    ServerGeoObjectIF parent = service.getGeoObject(this.getParentId(), this.getParentTypeCode());
    ServerGeoObjectIF child = service.getGeoObject(this.getChildId(), this.getChildTypeCode());
    ServerHierarchyType ht = ServerHierarchyType.get(this.getHierarchyTypeCode());

    ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanRemoveChild(ht.getOrganization().getCode(), parent.getType(), child.getType());
    
//    service.removeChild(parent, child, this.getHierarchyTypeCode(), null, null);
    
    throw new UnsupportedOperationException();
  }

  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);

    RemoveChildActionDTO acaDTO = (RemoveChildActionDTO) dto;

    this.setParentId(acaDTO.getParentCode());
    this.setParentTypeCode(acaDTO.getParentTypeCode());
    this.setChildId(acaDTO.getChildCode());
    this.setChildTypeCode(acaDTO.getChildTypeCode());
    this.setHierarchyTypeCode(acaDTO.getHierarchyCode());
  }

  @Override
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(RemoveChildAction.class, new ActionJsonAdapters.RemoveChildActionSerializer());

    return (JsonObject) builder.create().toJsonTree(this);
  }

  @Override
  public void buildFromJson(JSONObject joAction)
  {
    super.buildFromJson(joAction);

    this.setChildTypeCode(joAction.getString(RemoveChildAction.CHILDTYPECODE));
    this.setChildId(joAction.getString(RemoveChildAction.CHILDID));
    this.setParentId(joAction.getString(RemoveChildAction.PARENTID));
    this.setParentTypeCode(joAction.getString(RemoveChildAction.PARENTTYPECODE));
    this.setHierarchyTypeCode(joAction.getString(RemoveChildAction.HIERARCHYTYPECODE));
  }

  @Override
  protected String getMessage()
  {
    ServerGeoObjectType parentType = ServerGeoObjectType.get(this.getParentTypeCode());
    ServerGeoObjectType childType = ServerGeoObjectType.get(this.getChildTypeCode());
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(this.getHierarchyTypeCode());

    String message = LocalizationFacade.localize("change.request.email.remove.child");
    message = message.replaceAll("\\{0\\}", this.getChildId());
    message = message.replaceAll("\\{1\\}", childType.getLabel().getValue(Session.getCurrentLocale()));
    message = message.replaceAll("\\{2\\}", this.getParentId());
    message = message.replaceAll("\\{3\\}", parentType.getLabel().getValue(Session.getCurrentLocale()));
    message = message.replaceAll("\\{4\\}", hierarchyType.getLabel().getValue(Session.getCurrentLocale()));

    return message;
  }

  @Override
  public void apply()
  {
    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    ServerGeoObjectIF parent = service.getGeoObject(this.getParentId(), this.getParentTypeCode());
    ServerGeoObjectIF child = service.getGeoObject(this.getChildId(), this.getChildTypeCode());
    ServerHierarchyType ht = ServerHierarchyType.get(this.getHierarchyTypeCode());

    ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanRemoveChildCR(ht.getOrganization().getCode(), parent.getType(), child.getType());

    super.apply();
  }
}
