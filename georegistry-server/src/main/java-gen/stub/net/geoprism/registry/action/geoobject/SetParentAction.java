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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Session;

import net.geoprism.registry.action.ActionJsonAdapters;
import net.geoprism.registry.action.tree.RemoveChildAction;
import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.ChangeRequestPermissionService;
import net.geoprism.registry.permission.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime.ServerParentTreeNodeOverTimeDeserializer;

public class SetParentAction extends SetParentActionBase
{
  private static final long            serialVersionUID           = 876924243;
  
  private static final Logger logger = LoggerFactory.getLogger(RemoveChildAction.class);

  public SetParentAction()
  {
    super();
  }
  
  public static class ReferencesGOTGeoObjectNullDeserializer extends ServerParentTreeNodeOverTimeDeserializer
  {
    public ReferencesGOTGeoObjectNullDeserializer(ServerGeoObjectType type)
    {
      super(type);
    }
    
    @Override
    protected ServerGeoObjectIF deserializeGeoObject(JsonObject go, String goTypeCode, final JsonDeserializationContext context)
    {      
      return null;
    }
  }

  @Override
  public void execute()
  {
    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    ServerGeoObjectIF child = service.getGeoObjectByCode(this.getChildCode(), this.getChildTypeCode());

    ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(child.getType(), this.getJson());

    service.setParents(child, ptnOt);
  }

  @Override
  public void apply()
  {
    // Important to remember that the child may or may not exist at this point (so we can't fetch it from the DB here)
    
    ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(this.getChildTypeCode()).get();

    ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(type, this.getJson());

    ptnOt.enforceUserHasPermissionSetParents(this.getChildTypeCode(), true);

    super.apply();
  }

  @Override
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(SetParentAction.class, new ActionJsonAdapters.SetParentActionSerializer());

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
      this.setChildTypeCode(joAction.getString(SetParentAction.CHILDTYPECODE));
      this.setChildCode(joAction.getString(SetParentAction.CHILDCODE));
      this.setJson(joAction.getJSONArray(SetParentAction.JSON).toString());
    }
  }

  @Override
  protected String getMessage()
  {
    ServerGeoObjectType childType = ServerGeoObjectType.get(this.getChildTypeCode());

    String message = LocalizationFacade.localize("change.request.email.set.parent");
    message = message.replaceAll("\\{0\\}", childType.getLabel().getValue(Session.getCurrentLocale()));
    message = message.replaceAll("\\{1\\}", this.getChildCode());

    return message;
  }

}
