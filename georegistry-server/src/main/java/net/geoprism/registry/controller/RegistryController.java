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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.json.JSONArray;
import org.json.JSONException;

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

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.permission.PermissionContext;
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

  // /**
  // * Submits a change request to the GeoRegistry. These actions will be
  // reviewed
  // * by an Administrator and if the actions are approved they may be executed
  // * and accepted as formal changes to the GeoRegistry.
  // *
  // * @param request
  // * @param uid
  // * @return
  // * @throws JSONException
  // */
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url
  // = RegistryUrls.SUBMIT_CHANGE_REQUEST)
  // public ResponseIF submitChangeRequest(ClientRequestIF request,
  // @RequestParamter(name = RegistryUrls.SUBMIT_CHANGE_REQUEST_PARAM_ACTIONS)
  // String actions) throws JSONException
  // {
  // new ChangeRequestService().submitChangeRequest(request.getSessionId(),
  // actions);
  //
  // return new RestResponse();
  // }
  /**
   * Returns an OauthServer configuration with the specified id. If an id is not
   * provided, this endpoint will return all configurations (in your
   * organization).
   * 
   * @param request
   * @param id
   * @return A json array of serialized OauthServer configurations.
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "oauth/get")
  public ResponseIF oauthGetAll(ClientRequestIF request, @RequestParamter(name = "id") String id) throws JSONException
  {
    String json = this.registryService.oauthGetAll(request.getSessionId(), id);

    return new RestBodyResponse(json);
  }

  /**
   * Returns information which is available to public users (without
   * permissions) which will allow them to login as an oauth user.
   * 
   * @param request
   * @param id
   * @return A json array of OauthServer configurations with only publicly
   *         available information.
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "oauth/get-public")
  public ResponseIF oauthGetPublic(ClientRequestIF request, @RequestParamter(name = "id") String id) throws JSONException
  {
    String json = this.registryService.oauthGetPublic(request.getSessionId(), id);

    return new RestBodyResponse(json);
  }


  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE)
  // public ResponseIF updateAttributeType(ClientRequestIF request,
  // @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE_PARAM) String geoObjTypeId,
  // @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE_TYPE_PARAM) String
  // attributeType)
  // {
  // AttributeType attrType =
  // this.registryService.updateAttributeType(request.getSessionId(),
  // geoObjTypeId, attributeType);
  //
  // return new RestBodyResponse(attrType.toJSON());
  // }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE)
  // public ResponseIF deleteAttributeType(ClientRequestIF request,
  // @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE_PARAM) String geoObjTypeId,
  // @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE_TYPE_PARAM) String
  // attributeName)
  // {
  // this.registryService.deleteAttributeType(request.getSessionId(),
  // geoObjTypeId, attributeName);
  //
  // return new RestResponse();
  // }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM)
  // public ResponseIF createTerm(ClientRequestIF request, @RequestParamter(name
  // = RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM_PARENT_PARAM) String
  // parentTermCode, @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM_PARAM) String termJSON)
  // {
  // Term term = this.registryService.createTerm(request.getSessionId(),
  // parentTermCode, termJSON);
  //
  // return new RestBodyResponse(term.toJSON());
  // }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_UPDATE_TERM)
  // public ResponseIF updateTerm(ClientRequestIF request, @RequestParamter(name
  // = RegistryUrls.GEO_OBJECT_TYPE_UPDATE_TERM_PARAM) String termJSON)
  // {
  // Term term = this.registryService.updateTerm(request.getSessionId(),
  // termJSON);
  //
  // return new RestBodyResponse(term.toJSON());
  // }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_DELETE_TERM)
  // public ResponseIF deleteTerm(ClientRequestIF request, @RequestParamter(name
  // = RegistryUrls.GEO_OBJECT_TYPE_DELETE_TERM_PARAM) String termCode)
  // {
  // this.registryService.deleteTerm(request.getSessionId(), termCode);
  //
  // return new RestResponse();
  // }

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
