/**
 *
 */
package net.geoprism.registry.test.curation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.registry.controller.CurationController;
import net.geoprism.registry.controller.CurationController.VersionIdBody;
import net.geoprism.registry.test.AbstractTestClient;

@Component
public class CurationControllerWrapper extends AbstractTestClient
{
  @Autowired
  private CurationController controller;

  public JsonObject details(String historyId, Boolean onlyUnresolved, Integer pageSize, Integer pageNumber)
  {
    return JsonParser.parseString(responseToString(this.controller.details(historyId, onlyUnresolved, pageSize, pageNumber))).getAsJsonObject();
  }

  public JsonObject page(String historyId, Boolean onlyUnresolved, Integer pageSize, Integer pageNumber)
  {
    return JsonParser.parseString(responseToString(this.controller.page(historyId, onlyUnresolved, pageSize, pageNumber))).getAsJsonObject();
  }

  public JsonObject curate(String listTypeVersionId)
  {
    VersionIdBody body = new VersionIdBody();
    body.setListTypeVersionId(listTypeVersionId);

    return JsonParser.parseString(responseToString(this.controller.curate(body))).getAsJsonObject();
  }

}
