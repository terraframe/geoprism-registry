package net.geoprism.registry.service;

import java.util.List;

import org.json.JSONException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationRAException;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.view.Page;

public class ExternalSystemService
{
  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, Integer pageNumber, Integer pageSize) throws JSONException
  {
    long count = ExternalSystem.getCount();
    List<ExternalSystem> results = ExternalSystem.getExternalSystemsForOrg(pageNumber, pageSize);

    return new Page<ExternalSystem>(count, pageNumber, pageSize, results).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String json) throws JSONException
  {
    JsonElement element = new JsonParser().parse(json);

    ExternalSystem system = ExternalSystem.desieralize(element.getAsJsonObject());
    system.apply();

    return system.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid) throws JSONException
  {
    ExternalSystem system = ExternalSystem.get(oid);

    return system.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    ExternalSystem system = ExternalSystem.get(oid);
    Organization organization = system.getOrganization();

    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      ServiceFactory.getRolePermissionService().enforceRA(session.getUser(), organization.getCode());
    }

    system.delete();
  }
}
