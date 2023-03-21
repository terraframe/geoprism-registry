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

import java.util.Arrays;
import java.util.Set;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.tree.AddChildActionDTO;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataCache;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Session;

import net.geoprism.registry.action.ActionJsonAdapters;
import net.geoprism.registry.action.ChangeRequestPermissionService;
import net.geoprism.registry.action.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.service.ServerGeoObjectService;
import net.geoprism.registry.service.ServiceFactory;

public class AddChildAction extends AddChildActionBase
{
  private static final long serialVersionUID = -325315873;
  
  private static final Logger logger = LoggerFactory.getLogger(AddChildAction.class);

  @Override
  public void execute()
  {
    ServerGeoObjectIF parent = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).getGeoObject(this.getParentId(), this.getParentTypeCode());
    ServerGeoObjectIF child = new ServerGeoObjectService().getGeoObject(this.getChildId(), this.getChildTypeCode());
    ServerHierarchyType ht = ServerHierarchyType.get(this.getHierarchyTypeCode());

    ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(), parent.getType(), child.getType());

    parent.addChild(child, ht);
  }

  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);

    AddChildActionDTO acaDTO = (AddChildActionDTO) dto;

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
    builder.registerTypeAdapter(AddChildAction.class, new ActionJsonAdapters.AddChildActionSerializer());

    return (JsonObject) builder.create().toJsonTree(this);
  }

  @Override
  public void buildFromJson(JSONObject joAction)
  {
    super.buildFromJson(joAction);
    
    Set<ChangeRequestPermissionAction> perms = new ChangeRequestPermissionService().getPermissions(this.getAllRequest().next());
    
    if (perms.containsAll(Arrays.asList(
        ChangeRequestPermissionAction.WRITE_DETAILS
      )))
    {
      this.setChildTypeCode(joAction.getString(AddChildAction.CHILDTYPECODE));
      this.setChildId(joAction.getString(AddChildAction.CHILDID));
      this.setParentId(joAction.getString(AddChildAction.PARENTID));
      this.setParentTypeCode(joAction.getString(AddChildAction.PARENTTYPECODE));
      this.setHierarchyTypeCode(joAction.getString(AddChildAction.HIERARCHYTYPECODE));
    }
  }

  @Override
  protected String getMessage()
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();
    MetadataCache cache = adapter.getMetadataCache();

    ServerGeoObjectType parentType = ServerGeoObjectType.get(this.getParentTypeCode());
    ServerGeoObjectType childType = ServerGeoObjectType.get(this.getChildTypeCode());
    HierarchyType hierarchyType = cache.getHierachyType(this.getHierarchyTypeCode()).get();

    String message = LocalizationFacade.localize("change.request.email.add.child");
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
    ServerGeoObjectIF parent = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).getGeoObject(this.getParentId(), this.getParentTypeCode());
    ServerGeoObjectIF child = new ServerGeoObjectService().getGeoObject(this.getChildId(), this.getChildTypeCode());
    ServerHierarchyType ht = ServerHierarchyType.get(this.getHierarchyTypeCode());

    ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChildCR(ht.getOrganization().getCode(), parent.getType(), child.getType());

    super.apply();
  }
}
