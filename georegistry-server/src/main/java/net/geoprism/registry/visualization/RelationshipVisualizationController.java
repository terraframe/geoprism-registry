package net.geoprism.registry.visualization;

import com.google.gson.JsonElement;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.registry.GeoRegistryUtil;

@Controller(url = "relationship-visualization")
public class RelationshipVisualizationController
{
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "tree")
  public ResponseIF tree(ClientRequestIF request, @RequestParamter(name = "mdEdgeOid") String mdEdgeOid, @RequestParamter(name = "geoObjectCode") String geoObjectCode, @RequestParamter(name = "geoObjectTypeCode") String geoObjectTypeCode, @RequestParamter(name = "date") String sDate)
  {
    JsonElement json = new RelationshipVisualizationService().tree(request.getSessionId(), GeoRegistryUtil.parseDate(sDate), mdEdgeOid, geoObjectCode, geoObjectTypeCode);

    return new RestBodyResponse(json);
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "relationships")
  public ResponseIF relationships(ClientRequestIF request, @RequestParamter(name = "geoObjectTypeCode") String geoObjectTypeCode)
  {
    JsonElement json = new RelationshipVisualizationService().getRelationships(request.getSessionId(), geoObjectTypeCode);

    return new RestBodyResponse(json);
  }
}
