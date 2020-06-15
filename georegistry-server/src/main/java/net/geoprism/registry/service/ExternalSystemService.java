package net.geoprism.registry.service;

import java.util.List;

import org.json.JSONException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationRAException;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.view.Page;

public class ExternalSystemService
{
  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, String orgCode, Integer pageNumber, Integer pageSize) throws JSONException
  {
    Organization organization = Organization.getByCode(orgCode);

    long count = ExternalSystem.getCount(organization);
    List<ExternalSystem> results = ExternalSystem.getExternalSystemsForOrg(organization, pageNumber, pageSize);

    return new Page<ExternalSystem>(count, pageNumber, pageSize, results).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String orgCode, String json) throws JSONException
  {
    Organization organization = Organization.getByCode(orgCode);
    boolean isRA = ServiceFactory.getRolePermissionService().isRA(Session.getCurrentSession().getUser(), orgCode);

    if (!isRA)
    {
      OrganizationRAException exception = new OrganizationRAException();
      exception.setOrganizationLabel(organization.getDisplayLabel().getValue());
      throw exception;
    }

    JsonElement element = new JsonParser().parse(json);

    ExternalSystem system = ExternalSystem.desieralize(element.getAsJsonObject());
    system.setOrganization(organization);
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

    boolean isRA = ServiceFactory.getRolePermissionService().isRA(Session.getCurrentSession().getUser(), organization.getCode());

    if (!isRA)
    {
      OrganizationRAException exception = new OrganizationRAException();
      exception.setOrganizationLabel(organization.getDisplayLabel().getValue());
      throw exception;
    }

    system.delete();
  }
}
