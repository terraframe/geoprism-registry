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
package net.geoprism.registry.action.tree;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.tree.RemoveChildActionDTO;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataCache;
import org.json.JSONObject;

import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;

import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServerGeoObjectService;
import net.geoprism.registry.service.ServiceFactory;

public class RemoveChildAction extends RemoveChildActionBase
{
  private static final long serialVersionUID = -165581118;

  @Override
  public void execute()
  {
    ServerGeoObjectService service = new ServerGeoObjectService();

    ServerGeoObjectIF parent = service.getGeoObject(this.getParentId(), this.getParentTypeCode());
    ServerGeoObjectIF child = service.getGeoObject(this.getChildId(), this.getChildTypeCode());

    parent.removeChild(child, this.getHierarchyTypeCode());
  }

  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);

    RemoveChildActionDTO acaDTO = (RemoveChildActionDTO) dto;

    this.setParentId(acaDTO.getParentId());
    this.setParentTypeCode(acaDTO.getParentTypeCode());
    this.setChildId(acaDTO.getChildId());
    this.setChildTypeCode(acaDTO.getChildTypeCode());
    this.setHierarchyTypeCode(acaDTO.getHierarchyCode());
  }

  @Override
  public JSONObject serialize()
  {
    JSONObject jo = super.serialize();
    jo.put(RemoveChildAction.CHILDID, this.getChildId());
    jo.put(RemoveChildAction.CHILDTYPECODE, this.getChildTypeCode());
    jo.put(RemoveChildAction.PARENTID, this.getParentId());
    jo.put(RemoveChildAction.PARENTTYPECODE, this.getParentTypeCode());
    jo.put(RemoveChildAction.HIERARCHYTYPECODE, this.getHierarchyTypeCode());

    return jo;
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
    RegistryAdapter adapter = ServiceFactory.getAdapter();
    MetadataCache cache = adapter.getMetadataCache();

    ServerGeoObjectType parentType = ServerGeoObjectType.get(this.getParentTypeCode());
    ServerGeoObjectType childType = ServerGeoObjectType.get(this.getChildTypeCode());
    HierarchyType hierarchyType = cache.getHierachyType(this.getHierarchyTypeCode()).get();

    String message = LocalizationFacade.getFromBundles("change.request.email.remove.child");
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
    ServerGeoObjectService service = new ServerGeoObjectService();
    
    ServerGeoObjectIF parent = service.getGeoObject(this.getParentId(), this.getParentTypeCode());
    ServerGeoObjectIF child = service.getGeoObject(this.getChildId(), this.getChildTypeCode());
    ServerHierarchyType ht = ServerHierarchyType.get(this.getHierarchyTypeCode());
    
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      SingleActorDAOIF actor = Session.getCurrentSession().getUser();
      
      ht.enforceActorHasRelationshipPermission(actor, parent.getType().getCode(), child.getType().getCode(), true);
    }
    
    super.apply();
  }
}
