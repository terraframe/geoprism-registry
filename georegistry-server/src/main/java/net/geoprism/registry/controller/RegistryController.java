/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.controller;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;

import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.service.AccountService;
import net.geoprism.registry.service.ExternalSystemService;
import net.geoprism.registry.service.OrganizationService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

@Controller(url = RegistryUrls.REGISTRY_CONTROLLER_URL)
public class RegistryController
{
  public static final String JSP_DIR   = "/WEB-INF/";

  public static final String INDEX_JSP = "net/geoprism/registry/index.jsp";

  private RegistryService    registryService;

  public RegistryController()
  {
    this.registryService = RegistryService.getInstance();
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF manage()
  {
    ViewResponse resp = new ViewResponse(JSP_DIR + INDEX_JSP);

    String customFont = GeoregistryProperties.getCustomFont();
    if (customFont != null && customFont.length() > 0)
    {
      resp.set("customFont", customFont);
    }

    return resp;
  }

  /**
   * Create the {@link HierarchyType} from the given JSON.
   * 
   * @param sessionId
   * @param htJSON
   *          JSON of the {@link HierarchyType} to be created.
   * @throws IOException
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "import-types")
  public ResponseIF importTypes(ClientRequestIF request, 
      @RequestParamter(name = "orgCode", required = true) String orgCode, 
      @RequestParamter(name = "file", required = true) MultipartFileParameter file) throws IOException
  {
    try (InputStream istream = file.getInputStream())
    {
      ServiceFactory.getRegistryService().importTypes(request.getSessionId(), orgCode, istream);

      return new RestResponse();
    }
  }

  /**
   * Returns a map with a list of all the types, all of the hierarchies, and all
   * of the locales currently installed in the system. This endpoint is used to
   * populate the hierarchy manager.
   * 
   * @param request
   * @return
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "init")
  public ResponseIF init(ClientRequestIF request)
  {
    return new RestBodyResponse(this.registryService.initHierarchyManager(request.getSessionId()));
  }

  @Endpoint(url = "init-settings", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF initSettings(ClientRequestIF request) throws ParseException
  {
    OrganizationDTO[] orgs = new OrganizationService().getOrganizations(request.getSessionId(), null); // TODO : This violates autowiring principles
    JsonArray jaLocales = this.registryService.getLocales(request.getSessionId());
    JsonObject esPage = new ExternalSystemService().page(request.getSessionId(), 1, 10);
    JsonObject sraPage = JsonParser.parseString(AccountService.getInstance().getSRAs(request.getSessionId(), 1, 10)).getAsJsonObject();
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    JsonObject settingsView = new JsonObject();

    JsonArray orgsJson = new JsonArray();
    for (OrganizationDTO org : orgs)
    {
      orgsJson.add(org.toJSON(serializer));
    }
    settingsView.add("organizations", orgsJson);

    settingsView.add("locales", jaLocales);

    settingsView.add("externalSystems", esPage);

    settingsView.add("sras", sraPage);

    return new RestBodyResponse(settingsView);
  }

}
