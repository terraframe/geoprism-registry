package net.geoprism.registry.test.curation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;

import net.geoprism.registry.curation.CurationController;
import net.geoprism.registry.test.TestControllerWrapper;
import net.geoprism.registry.test.TestRegistryAdapterClient;

public class CurationControllerWrapper extends TestControllerWrapper
{

  private CurationController controller = new CurationController();
  
  public CurationControllerWrapper(TestRegistryAdapterClient adapter, ClientRequestIF clientRequest)
  {
    super(adapter, clientRequest);
  }
  
  public JsonObject details(String historyId, Boolean onlyUnresolved, Integer pageSize, Integer pageNumber)
  {
    return JsonParser.parseString(responseToString(this.controller.details(this.clientRequest, historyId, onlyUnresolved, pageSize, pageNumber))).getAsJsonObject();
  }
  
  public JsonObject page(String historyId, Boolean onlyUnresolved, Integer pageSize, Integer pageNumber)
  {
    return JsonParser.parseString(responseToString(this.controller.page(this.clientRequest, historyId, onlyUnresolved, pageSize, pageNumber))).getAsJsonObject();
  }
  
  public JsonObject curate(String listTypeVersionId)
  {
    return JsonParser.parseString(responseToString(this.controller.curate(this.clientRequest, listTypeVersionId))).getAsJsonObject();
  }

}
