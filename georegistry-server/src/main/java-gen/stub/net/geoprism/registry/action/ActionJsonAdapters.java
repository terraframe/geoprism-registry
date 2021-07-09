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
package net.geoprism.registry.action;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTimeJsonAdapters;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.SingleActor;
import com.runwaysdk.system.Users;

import net.geoprism.registry.action.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.SetParentAction;
import net.geoprism.registry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.registry.action.tree.AddChildAction;
import net.geoprism.registry.action.tree.RemoveChildAction;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.service.ChangeRequestService;
import net.geoprism.registry.service.ServiceFactory;

public class ActionJsonAdapters
{

  abstract public static class AbstractActionDeserializer
  {
    public AbstractAction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
      return null;
    }
  }
  
  abstract public static class AbstractActionSerializer
  {
    private ChangeRequestService service = new ChangeRequestService();
    
    private ChangeRequestPermissionService perms = new ChangeRequestPermissionService();
    
    public JsonElement serialize(AbstractAction action, Type typeOfSrc, JsonSerializationContext context)
    {
      DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      
      AllGovernanceStatus status = action.getApprovalStatus().get(0);

      JsonObject jo = new JsonObject();

      jo.addProperty(AbstractAction.OID, action.getOid());
      jo.addProperty("actionType", action.getType());
      jo.addProperty("actionLabel", action.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
      jo.addProperty(AbstractAction.CREATEACTIONDATE, format.format(action.getCreateActionDate()));
      jo.addProperty(AbstractAction.CONTRIBUTORNOTES, action.getContributorNotes());
      jo.addProperty(AbstractAction.MAINTAINERNOTES, action.getMaintainerNotes());
      jo.addProperty(AbstractAction.ADDITIONALNOTES, action.getAdditionalNotes());
      jo.addProperty(AbstractAction.APPROVALSTATUS, action.getApprovalStatus().get(0).getEnumName());
      jo.addProperty("statusLabel", status.getDisplayLabel());
      jo.addProperty(AbstractAction.CREATEACTIONDATE, format.format(action.getCreateActionDate()));
      
      ChangeRequestJsonAdapters.serializeCreatedBy(action.getCreatedBy(), jo);

      SingleActor decisionMaker = action.getDecisionMaker();

      if (decisionMaker != null && ( decisionMaker instanceof Users ))
      {
        jo.addProperty(AbstractAction.DECISIONMAKER, ( (Users) decisionMaker ).getUsername());
      }
      
      JsonArray jaDocuments = JsonParser.parseString(this.service.listDocumentsAction(Session.getCurrentSession().getOid(), action.getOid())).getAsJsonArray();
      jo.add("documents", jaDocuments);

      jo.add("permissions", this.serializePermissions(action, context));
      
      return jo;
    }
    
    protected JsonArray serializePermissions(AbstractAction action, JsonSerializationContext context)
    {
      Set<ChangeRequestPermissionAction> crPerms = this.perms.getPermissions(action.getAllRequest().next());
      
      return context.serialize(crPerms).getAsJsonArray();
    }
  }
  
  abstract public static class GeoObjectActionSerializer extends AbstractActionSerializer
  {
    abstract JsonObject getGeoObjectJson(AbstractAction action);
  
    public JsonElement serialize(AbstractAction action, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject jo = super.serialize(action, typeOfSrc, context).getAsJsonObject();
      
      jo.add(CreateGeoObjectAction.GEOOBJECTJSON, this.getGeoObjectJson(action));
      
      this.addGeoObjectType(action, jo);
      
      return jo;
    }
    
    private void addGeoObjectType(AbstractAction action, JsonObject object)
    {
      JsonObject json = this.getGeoObjectJson(action);
      
      String typeCode = GeoObjectOverTimeJsonAdapters.GeoObjectDeserializer.getTypeCode(json.toString());
      
      Optional<GeoObjectType> op = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(typeCode);
  
      GeoObjectType got;
      if (op.isPresent())
      {
        got = op.get();
      }
      else
      {
        got = new GeoObjectType(typeCode, GeometryType.POLYGON, new LocalizedValue(typeCode), new LocalizedValue(""), false, "", ServiceFactory.getAdapter());
      }
      
      object.add("geoObjectType", got.toJSON());
    }
  }
  
  public static class CreateGeoObjectActionSerializer extends GeoObjectActionSerializer implements JsonSerializer<CreateGeoObjectAction>
  {
    @Override
    public JsonElement serialize(CreateGeoObjectAction action, Type typeOfSrc, JsonSerializationContext context)
    {
      return super.serialize(action, typeOfSrc, context);
    }

    @Override
    JsonObject getGeoObjectJson(AbstractAction action)
    {
      return JsonParser.parseString(((CreateGeoObjectAction)action).getGeoObjectJson()).getAsJsonObject();
    }
  }
  
  public static class UpdateGeoObjectActionSerializer extends GeoObjectActionSerializer implements JsonSerializer<UpdateGeoObjectAction>
  {
    @Override
    public JsonElement serialize(UpdateGeoObjectAction action, Type typeOfSrc, JsonSerializationContext context)
    {
      return super.serialize(action, typeOfSrc, context);
    }

    @Override
    JsonObject getGeoObjectJson(AbstractAction action)
    {
      return JsonParser.parseString(((UpdateGeoObjectAction)action).getGeoObjectJson()).getAsJsonObject();
    }
  }
  
  public static class SetParentActionSerializer extends AbstractActionSerializer implements JsonSerializer<SetParentAction>
  {
    @Override
    public JsonElement serialize(SetParentAction action, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject jo = super.serialize(action, typeOfSrc, context).getAsJsonObject();
      
      jo.add(SetParentAction.JSON, JsonParser.parseString(action.getJson()));
      jo.addProperty(SetParentAction.CHILDTYPECODE, action.getChildTypeCode());
      jo.addProperty(SetParentAction.CHILDCODE, action.getChildCode());
      
      return jo;
    }
  }
  
  public static class RemoveChildActionSerializer extends AbstractActionSerializer implements JsonSerializer<RemoveChildAction>
  {
    @Override
    public JsonElement serialize(RemoveChildAction action, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject jo = super.serialize(action, typeOfSrc, context).getAsJsonObject();
      
      jo.addProperty(RemoveChildAction.CHILDID, action.getChildId());
      jo.addProperty(RemoveChildAction.CHILDTYPECODE, action.getChildTypeCode());
      jo.addProperty(RemoveChildAction.PARENTID, action.getParentId());
      jo.addProperty(RemoveChildAction.PARENTTYPECODE, action.getParentTypeCode());
      jo.addProperty(RemoveChildAction.HIERARCHYTYPECODE, action.getHierarchyTypeCode());
      
      return jo;
    }
  }
  
  public static class AddChildActionSerializer extends AbstractActionSerializer implements JsonSerializer<AddChildAction>
  {
    @Override
    public JsonElement serialize(AddChildAction action, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject jo = super.serialize(action, typeOfSrc, context).getAsJsonObject();
      
      jo.addProperty(AddChildAction.CHILDID, action.getChildId());
      jo.addProperty(AddChildAction.CHILDTYPECODE, action.getChildTypeCode());
      jo.addProperty(AddChildAction.PARENTID, action.getParentId());
      jo.addProperty(AddChildAction.PARENTTYPECODE, action.getParentTypeCode());
      jo.addProperty(AddChildAction.HIERARCHYTYPECODE, action.getHierarchyTypeCode());
      
      return jo;
    }
  }

}
