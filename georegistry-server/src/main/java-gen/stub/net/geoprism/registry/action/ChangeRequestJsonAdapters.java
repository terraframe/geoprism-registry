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
package net.geoprism.registry.action;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.SingleActor;
import com.runwaysdk.system.Users;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.action.ChangeRequest.ChangeRequestType;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.cache.ServerMetadataCache;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GPRHierarchyTypeBusinessService;
import net.geoprism.registry.service.permission.ChangeRequestPermissionService;
import net.geoprism.registry.service.permission.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.service.request.ChangeRequestService;

public class ChangeRequestJsonAdapters
{
  public static void serializeCreatedBy(SingleActor actor, JsonObject jo)
  {
    if (actor instanceof Users)
    {
      Users user = (Users) actor;

      user.getUsername();

      jo.addProperty(ChangeRequest.CREATEDBY, user.getUsername());

      if (user instanceof GeoprismUser)
      {
        jo.addProperty("email", ( (GeoprismUser) user ).getEmail());
        jo.addProperty("phoneNumber", ( (GeoprismUser) user ).getPhoneNumber());
      }
      else
      {
        jo.addProperty("email", "");
        jo.addProperty("phoneNumber", "");
      }
    }
    else
    {
      jo.addProperty(ChangeRequest.CREATEDBY, actor.getKey());
      jo.addProperty("email", "");
      jo.addProperty("phoneNumber", "");
    }
  }

  public static class ChangeRequestDeserializer implements JsonDeserializer<ChangeRequest>
  {
    @Override
    public ChangeRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
      JsonObject jo = json.getAsJsonObject();

      ChangeRequest cr = new ChangeRequest();

      if (jo.has(ChangeRequest.MAINTAINERNOTES))
      {
        cr.setMaintainerNotes(jo.get(ChangeRequest.MAINTAINERNOTES).getAsString());
      }

      if (jo.has(ChangeRequest.ADDITIONALNOTES))
      {
        cr.setAdditionalNotes(jo.get(ChangeRequest.ADDITIONALNOTES).getAsString());
      }

      return cr;
    }
  }

  public static class ChangeRequestSerializer implements JsonSerializer<ChangeRequest>
  {
    private boolean                        hasCreate = false;

    private ServerGeoObjectType            type      = null;

    private GPRGeoObjectBusinessServiceIF  objectService;

    private GPRHierarchyTypeBusinessService hierarchyService;
    
    private ChangeRequestService           service;

    private ChangeRequestPermissionService perms;

    public ChangeRequestSerializer()
    {
      this.service = ServiceFactory.getBean(ChangeRequestService.class);
      this.perms = ServiceFactory.getBean(ChangeRequestPermissionService.class);
      this.objectService = ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class);
      this.hierarchyService = ServiceFactory.getBean(GPRHierarchyTypeBusinessService.class);
    }

    @Override
    public JsonElement serialize(ChangeRequest cr, Type typeOfSrc, JsonSerializationContext context)
    {
      final ServerMetadataCache cache = ServiceFactory.getMetadataCache();
      DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      type = cr.getGeoObjectType();

      AllGovernanceStatus status = cr.getApprovalStatus().get(0);

      JsonObject object = new JsonObject();
      object.addProperty(ChangeRequest.OID, cr.getOid());
      object.addProperty(ChangeRequest.CREATEDATE, format.format(cr.getCreateDate()));
      object.addProperty(ChangeRequest.APPROVALSTATUS, status.getEnumName());
      object.addProperty(ChangeRequest.MAINTAINERNOTES, cr.getMaintainerNotes());
      object.addProperty(ChangeRequest.CONTRIBUTORNOTES, cr.getContributorNotes());
      object.addProperty(ChangeRequest.ADDITIONALNOTES, cr.getAdditionalNotes());
      object.addProperty("statusLabel", status.getDisplayLabel());

      ChangeRequestJsonAdapters.serializeCreatedBy(cr.getCreatedBy(), object);

      if (Session.getCurrentSession() != null)
      {
        JsonArray jaDocuments = JsonParser.parseString(this.service.listDocumentsCR(Session.getCurrentSession().getOid(), cr.getOid())).getAsJsonArray();
        object.add("documents", jaDocuments);
      }

      object.add("permissions", this.serializePermissions(cr, context));

      object.add("actions", this.serializeActions(cr));

      addCurrent(cr, object, context);

      object.addProperty("type", this.hasCreate ? ChangeRequestType.CreateGeoObject.name() : ChangeRequestType.UpdateGeoObject.name());

      // Create and populate the "organization". This object will contain a code
      // and label for the Organization in the CR.
      JsonObject organization = new JsonObject();
      organization.addProperty("code", cr.getOrganizationCode());
      if (cache.getOrganization(cr.getOrganizationCode()).isPresent())
      {
        organization.addProperty("label", cache.getOrganization(cr.getOrganizationCode()).get().getDisplayLabel().getValue());
      }
      object.add("organization", organization);

      // Create and populate the "geoObjectType". This object will contain a
      // code and label for the GeoObjectType in the CR.
      JsonObject geoObjectType = new JsonObject();
      geoObjectType.addProperty("code", cr.getGeoObjectTypeCode());
      if (type != null)
      {
        geoObjectType.addProperty("label", type.getLabel().getValue());
      }
      else
      {
        geoObjectType.addProperty("label", cr.getGeoObjectTypeCode());
      }
      object.add("geoObjectType", geoObjectType);

      // Create and populate the "geoObject". This object will contain a code
      // and label for the GeoObject in the CR.
      JsonObject geoObject = new JsonObject();
      geoObject.addProperty("code", cr.getGeoObjectCode());
      geoObject.addProperty("label", cr.getGeoObjectDisplayLabel().getValue());
      object.add("geoObject", geoObject);

      return object;
    }

    private void addCurrent(ChangeRequest cr, JsonObject object, JsonSerializationContext context)
    {
      GeoObjectType got;

      JsonObject current = new JsonObject();

      // Add serialized GeoObjectType
      if (type == null)
      {
        got = new GeoObjectType(cr.getGeoObjectTypeCode(), GeometryType.POLYGON, new LocalizedValue(cr.getGeoObjectTypeCode()), new LocalizedValue(""), false, "", ServiceFactory.getAdapter());
      }
      else
      {
        got = this.type.toDTO();
      }

      current.add("geoObjectType", got.toJSON());

      if (!hasCreate)
      {
        VertexServerGeoObject go = cr.getGeoObject();

        if (go != null)
        {
          // Add serialized GeoObject to current
          current.add("geoObject", context.serialize(this.objectService.toGeoObjectOverTime(go)));

          // Add hierarchies
          current.add("hierarchies", this.hierarchyService.getHierarchiesForGeoObjectOverTime(go.getCode(), go.getType().getCode()));
        }
        else
        {
          cr.lock();
          cr.clearApprovalStatus();
          cr.addApprovalStatus(AllGovernanceStatus.INVALID);
          cr.apply();
        }
      }

      object.add("current", current);
    }

    protected JsonArray serializeActions(ChangeRequest cr)
    {
      JsonArray ja = new JsonArray();

      OIterator<? extends AbstractAction> actions = cr.getAllAction();

      for (AbstractAction action : actions)
      {
        if (action instanceof CreateGeoObjectAction)
        {
          hasCreate = true;
        }

        ja.add(action.toJson());
      }

      return ja;
    }

    protected JsonArray serializePermissions(ChangeRequest cr, JsonSerializationContext context)
    {
      Set<ChangeRequestPermissionAction> crPerms = this.perms.getPermissions(cr);

      return (JsonArray) context.serialize(crPerms);
    }
  }

}
