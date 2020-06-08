package net.geoprism.registry.controller;

import java.io.InputStream;
import java.util.Date;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;

import net.geoprism.registry.service.ServiceFactory;

@Controller(url = "geoobject")
public class GeoObjectController
{
  /**
   * Returns a paginated response of all GeoObjects matching the provided criteria.
   **/
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-all")
  public ResponseIF getAll(ClientRequestIF request, @RequestParamter(name = "typeCode") String typeCode,
      @RequestParamter(name = "hierarchyCode") String hierarchyCode, @RequestParamter(name = "updatedSince") Long updatedSince,
      @RequestParamter(name = "includeLevel") Boolean includeLevel, @RequestParamter(name = "format") String format,
      @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize)
  {
    Date dUpdatedSince = null;
    if (updatedSince != null)
    {
      dUpdatedSince = new Date(updatedSince);
    }
    
    InputStream is = ServiceFactory.getGeoObjectService().getAll(request.getSessionId(), typeCode, hierarchyCode, dUpdatedSince, includeLevel, format, pageNumber, pageSize);
    
    return new InputStreamResponse(is, "application/json", "get-all.json");
  }
}
