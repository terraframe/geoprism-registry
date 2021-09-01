/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.request.ServletRequestIF;

import net.geoprism.AccountController;
import net.geoprism.GeoprismUserDTO;
import net.geoprism.account.UserInviteDTO;
import net.geoprism.registry.account.RegistryAccountUtilDTO;
import net.geoprism.registry.service.AccountService;

@Controller(url = "registryaccount")
public class RegistryAccountController
{
  private AccountService accountService;

  public RegistryAccountController()
  {
    this.accountService = AccountService.getInstance();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize) throws JSONException
  {
    String json = this.accountService.page(request.getSessionId(), pageNumber, pageSize);

    return new RestBodyResponse(json);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-sras")
  public ResponseIF getSRAs(ClientRequestIF request, @RequestParamter(name = "pageNumber") Integer pageNumber, @RequestParamter(name = "pageSize") Integer pageSize) throws JSONException
  {
    String json = this.accountService.getSRAs(request.getSessionId(), pageNumber, pageSize);

    return new RestBodyResponse(json);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF getRolesForUser(ClientRequestIF request, @RequestParamter(name = "userOID") String userOID) throws JSONException
  {
    RegistryRole[] roles = this.accountService.getRolesForUser(request.getSessionId(), userOID);

    JsonArray rolesJSONArray = this.createRoleMap(roles);

    return new RestBodyResponse(rolesJSONArray);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    JSONObject user = this.accountService.lock(request.getSessionId(), oid);

    RegistryRole[] registryRoles = this.accountService.getRolesForUser(request.getSessionId(), oid);

    JsonArray rolesJSONArray = this.createRoleMap(registryRoles);

    RestResponse response = new RestResponse();
    response.set("user", user);
    response.set("roles", new JSONArray(rolesJSONArray.toString()));

    return response;
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF get(ClientRequestIF request) throws JSONException
  {
    JSONObject object = this.accountService.get(request.getSessionId());

    return new RestBodyResponse(object);
  }

  /**
   * Returns all roles associated with the given {@link OrganizationDTO} codes.
   * If no codes are provided then return all roles defined in the registry.
   * 
   * @param request
   * @param organizationCodes
   *          comma separated list of {@link OrganizationDTO} codes.
   * @return
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newInstance(ClientRequestIF request, @RequestParamter(name = "organizationCodes") String organizationCodes) throws JSONException
  {

    String[] orgCodeArray = null;

    if (organizationCodes != null)
    {
      JSONArray arr = new JSONArray(organizationCodes);
      orgCodeArray = new String[arr.length()];
      for (int i = 0; i < arr.length(); i++)
      {
        orgCodeArray[i] = arr.getString(i);
      }
    }
    else
    {
      orgCodeArray = new String[0];
    }

    GeoprismUserDTO user = UserInviteDTO.newUserInst(request);

    RegistryRole[] registryRoles = this.accountService.getRolesForOrganization(request.getSessionId(), orgCodeArray);
    JsonArray rolesJSONArray = this.createRoleMap(registryRoles);

    RestResponse response = new RestResponse();
    response.set("user", user);
    response.set("roles", new JSONArray(rolesJSONArray.toString()));

    return response;
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newUserInstance(ClientRequestIF request) throws JSONException
  {
    return new AccountController().newUserInstance(request);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    accountService.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    this.accountService.unlock(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "account") String account, @RequestParamter(name = "roleNames") String roleNames) throws JSONException
  {
    String[] roleNameArray = null;

    if (roleNames != null)
    {
      JSONArray arr = new JSONArray(roleNames);
      roleNameArray = new String[arr.length()];
      for (int i = 0; i < arr.length(); i++)
      {
        roleNameArray[i] = arr.getString(i);
      }
    }

    JSONObject user = this.accountService.apply(request.getSessionId(), account, roleNameArray);

    RegistryRole[] registryRoles = this.accountService.getRolesForUser(request.getSessionId(), user.getString(GeoprismUserDTO.OID));

    JsonArray rolesJSONArray = this.createRoleMap(registryRoles);

    RestResponse response = new RestResponse();
    response.set("user", user);
    response.set("roles", new JSONArray(rolesJSONArray.toString()));

    return response;
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF inviteUser(ClientRequestIF request, ServletRequestIF sr, @RequestParamter(name = "invite") String sInvite, @RequestParamter(name = "roleIds") String roleIds) throws JSONException
  {
    RegistryAccountUtilDTO.initiate(request, sInvite, roleIds, null);

    return new RestResponse();
  }

  /**
   * 
   * 
   * @param request
   * @param organizationCodes
   *          comma delimited list of registry codes. Returns all registry roles
   *          if empty.
   * @return
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newInvite(ClientRequestIF request, @RequestParamter(name = "organizationCodes") String organizationCodes) throws JSONException
  {
    String[] orgCodeArray = null;

    if (organizationCodes != null)
    {
      JSONArray arr = new JSONArray(organizationCodes);
      orgCodeArray = new String[arr.length()];
      for (int i = 0; i < arr.length(); i++)
      {
        orgCodeArray[i] = arr.getString(i);
      }
    }
    else
    {
      orgCodeArray = new String[0];
    }

    JSONObject user = new JSONObject();
    user.put("newInstance", true);

    RegistryRole[] registryRoles = this.accountService.getRolesForOrganization(request.getSessionId(), orgCodeArray);
    JsonArray rolesJSONArray = this.createRoleMap(registryRoles);

    RestResponse response = new RestResponse();
    response.set("user", user);
    response.set("roles", new JSONArray(rolesJSONArray.toString()));

    return response;
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF inviteComplete(ClientRequestIF request, @RequestParamter(name = "user") String user, @RequestParamter(name = "token") String token) throws JSONException
  {
    RegistryAccountUtilDTO.inviteComplete(request, token, user);

    return new RestResponse();
  }

  private JsonArray createRoleMap(RegistryRole[] roles)
  {
    JsonArray roleJSONArray = new JsonArray();

    for (RegistryRole role : roles)
    {
      roleJSONArray.add(role.toJSON());
    }

    return roleJSONArray;
  }
}
