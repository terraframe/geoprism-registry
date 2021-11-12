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

@Controller(url = "relationship-visualization")
public class RelationshipVisualizationController
{
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "hierarchy-data")
  public ResponseIF hierarchyData(ClientRequestIF request, @RequestParamter(name = "hierarchyCode") String hierarchyCode, @RequestParamter(name = "geoObjectCode") String geoObjectCode, @RequestParamter(name = "geoObjectTypeCode") String geoObjectTypeCode)
  {
    JsonElement json = new RelationshipVisualizationService().fetchHierarchyVisualizerData(request.getSessionId(), null, hierarchyCode, geoObjectCode, geoObjectTypeCode);

    return new RestBodyResponse(json);
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "graph-data")
  public ResponseIF graphData(ClientRequestIF request, @RequestParamter(name = "geoObjectCode") String geoObjectCode, @RequestParamter(name = "geoObjectTypeCode") String geoObjectTypeCode)
  {
    JsonElement json = new RelationshipVisualizationService().fetchGraphVisualizerData(request.getSessionId(), null, geoObjectCode, geoObjectTypeCode);

    return new RestBodyResponse(json);
  }
}
