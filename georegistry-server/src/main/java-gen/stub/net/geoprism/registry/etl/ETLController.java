package net.geoprism.registry.etl;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

@Controller(url = "etl")
public class ETLController
{
  protected ETLService service;
  
  public ETLController()
  {
    this.service = new ETLService();
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "import")
  public ResponseIF doImport(String sessionId, String json)
  {
    JSONObject config = this.service.doImport(sessionId, json);
    
    return new RestBodyResponse(config.toString());
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-active")
  public ResponseIF getActiveImports(String sessionId, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    JSONArray config = this.service.getActiveImports(sessionId, pageSize, pageNumber, sortAttr, isAscending);
    
    return new RestBodyResponse(config.toString());
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-completed")
  public ResponseIF getCompletedImports(String sessionId, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    JSONArray config = this.service.getCompletedImports(sessionId, pageSize, pageNumber, sortAttr, isAscending);
    
    return new RestBodyResponse(config.toString());
  }
  
}
