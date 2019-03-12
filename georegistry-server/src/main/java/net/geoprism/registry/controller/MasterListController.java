package net.geoprism.registry.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.service.MasterListService;
import net.geoprism.registry.service.RegistryService;

@Controller(url = "master-list")
public class MasterListController
{
  private MasterListService service;

  private RegistryService   registryService;

  public MasterListController()
  {
    this.service = new MasterListService();
    this.registryService = RegistryService.getInstance();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "list-all")
  public ResponseIF listAll(ClientRequestIF request)
  {
    JsonObject response = new JsonObject();
    response.add("lists", this.service.listAll(request.getSessionId()));
    response.add("locales", this.registryService.getLocales(request.getSessionId()));

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "create")
  public ResponseIF create(ClientRequestIF request, @RequestParamter(name = "list") String listJSON)
  {
    JsonParser parser = new JsonParser();
    JsonObject list = parser.parse(listJSON).getAsJsonObject();

    JsonObject response = this.service.create(request.getSessionId(), list);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

}
