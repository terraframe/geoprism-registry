package net.geoprism.registry.action;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.SingleActor;
import com.runwaysdk.system.Users;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.action.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.service.ChangeRequestService;

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
    }
    else
    {
      jo.addProperty(ChangeRequest.CREATEDBY, actor.getKey());
    }
  }

  public static class ChangeRequestDeserializer implements JsonDeserializer<ChangeRequest>
  {
    @Override
    public ChangeRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
      return null;
    }
  }
  
  public static class ChangeRequestSerializer implements JsonSerializer<ChangeRequest>
  {
    private ChangeRequestService service = new ChangeRequestService();
    
    private ChangeRequestPermissionService perms = new ChangeRequestPermissionService();
    
    @Override
    public JsonElement serialize(ChangeRequest cr, Type typeOfSrc, JsonSerializationContext context)
    {
      DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);

      
      AllGovernanceStatus status = cr.getApprovalStatus().get(0);
      
      JsonObject object = new JsonObject();
      object.addProperty(ChangeRequest.OID, cr.getOid());
      object.addProperty(ChangeRequest.CREATEDATE, format.format(cr.getCreateDate()));
      object.addProperty(ChangeRequest.APPROVALSTATUS, status.getEnumName());
      object.addProperty(ChangeRequest.MAINTAINERNOTES, cr.getMaintainerNotes());
      object.addProperty("statusLabel", status.getDisplayLabel());

      ChangeRequestJsonAdapters.serializeCreatedBy(cr.getCreatedBy(), object);
      
      JsonArray jaDocuments = JsonParser.parseString(this.service.listDocumentsCR(Session.getCurrentSession().getOid(), cr.getOid())).getAsJsonArray();
      object.add("documents", jaDocuments);
      
      object.add("permissions", this.serializePermissions(cr, context));
      
      return object;
    }
    
    protected JsonArray serializePermissions(ChangeRequest cr, JsonSerializationContext context)
    {
      Set<ChangeRequestPermissionAction> crPerms = this.perms.getPermissions(cr);
      
      return (JsonArray) context.serialize(crPerms);
    }
  }

}
