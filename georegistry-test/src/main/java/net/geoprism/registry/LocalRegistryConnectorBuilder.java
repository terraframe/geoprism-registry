/**
 *
 */
package net.geoprism.registry;

import org.apache.http.NameValuePair;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.registry.lpg.adapter.RegistryConnectorBuilderIF;
import net.geoprism.registry.lpg.adapter.RegistryConnectorIF;
import net.geoprism.registry.lpg.adapter.exception.BadServerUriException;
import net.geoprism.registry.lpg.adapter.exception.HTTPException;
import net.geoprism.registry.lpg.adapter.response.RegistryResponse;
import net.geoprism.registry.service.request.LabeledPropertyGraphTypeServiceIF;

public class LocalRegistryConnectorBuilder implements RegistryConnectorBuilderIF
{
  public static class LocalRegistryConnector implements RegistryConnectorIF
  {
    private LabeledPropertyGraphTypeServiceIF service;

    public LocalRegistryConnector(LabeledPropertyGraphTypeServiceIF service)
    {
      this.service = service;
    }

    @Override
    public String getServerUrl()
    {
      return "localhost";
    }

    @Override
    @Request
    public RegistryResponse httpGet(String url, NameValuePair... params) throws HTTPException, BadServerUriException
    {
      String sessionId = Session.getCurrentSession().getOid();

      if (url.endsWith("get-all"))
      {
        return new RegistryResponse(service.getAll(sessionId).toString(), 200);
      }
      else if (url.endsWith("get"))
      {
        JsonObject object = service.get(sessionId, params[0].getValue());
        String code = object.get(LabeledPropertyGraphType.CODE).getAsString();

        object.addProperty(LabeledPropertyGraphType.CODE, code + "_L");

        return new RegistryResponse(object.toString(), 200);
      }
      else if (url.endsWith("entry"))
      {
        return new RegistryResponse(service.getEntry(sessionId, params[0].getValue()).toString(), 200);
      }
      else if (url.endsWith("entries"))
      {
        return new RegistryResponse(service.getEntries(sessionId, params[0].getValue()).toString(), 200);
      }
      else if (url.endsWith("versions"))
      {
        return new RegistryResponse(service.getVersions(sessionId, params[0].getValue()).toString(), 200);
      }
      else if (url.endsWith("version"))
      {
        return new RegistryResponse(service.getVersion(sessionId, params[0].getValue(), true).toString(), 200);
      }
      else if (url.endsWith("data"))
      {
        return new RegistryResponse(service.getData(sessionId, params[0].getValue()).toString(), 200);
      }
      else if (url.endsWith("geo-objects"))
      {
        return new RegistryResponse(service.getGeoObjects(sessionId, params[0].getValue(), Long.valueOf(params[1].getValue()), Integer.valueOf(params[2].getValue())).toString(), 200);
      }
      else if (url.endsWith("edges"))
      {
        return new RegistryResponse(service.getEdges(sessionId, params[0].getValue(), Long.valueOf(params[1].getValue()), Integer.valueOf(params[2].getValue())).toString(), 200);
      }

      throw new BadServerUriException();
    }

    @Override
    public void close()
    {
      // Do nothing
    }

  }

  private LabeledPropertyGraphTypeServiceIF service;

  public LocalRegistryConnectorBuilder(LabeledPropertyGraphTypeServiceIF service)
  {
    this.service = service;
  }

  @Override
  public RegistryConnectorIF build(String url)
  {
    return new LocalRegistryConnector(this.service);
  }

}
