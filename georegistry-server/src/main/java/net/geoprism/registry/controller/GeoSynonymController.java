package net.geoprism.registry.controller;

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.service.GeoSynonymService;

@Controller(url = "geo-synonym")
public class GeoSynonymController
{
  private GeoSynonymService service = new GeoSynonymService();

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF createGeoEntitySynonym(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId, @RequestParamter(name = "label") String label) throws JSONException
  {
    JSONObject response = service.createGeoEntitySynonym(request.getSessionId(), entityId, label);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF deleteGeoEntitySynonym(ClientRequestIF request, @RequestParamter(name = "synonymId") String synonymId, @RequestParamter(name = "vOid") String vOid)
  {
    service.deleteGeoEntitySynonym(request.getSessionId(), synonymId, vOid);

    return new RestResponse();
  }

}
