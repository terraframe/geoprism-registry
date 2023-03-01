/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.mvc.NullConfiguration;
import com.runwaysdk.mvc.conversion.BusinessDTOToBasicJSON;

import net.geoprism.GeoprismUserDTO;
import net.geoprism.account.UserInviteDTO;
import net.geoprism.registry.controller.BusinessTypeController.OidBody;
import net.geoprism.registry.service.AccountService;
import net.geoprism.registry.spring.JsonArrayDeserializer;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class RegistryAccountController extends RunwaySpringController
{
  public static class InviteCompleteBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject user;

    @NotEmpty
    String     token;

    public JsonObject getUser()
    {
      return user;
    }

    public void setUser(JsonObject user)
    {
      this.user = user;
    }

    public String getToken()
    {
      return token;
    }

    public void setToken(String token)
    {
      this.token = token;
    }

  }

  public static class OrgUserBody
  {
    @JsonDeserialize(using = JsonArrayDeserializer.class)
    JsonArray organizationCodes;

    public JsonArray getOrganizationCodes()
    {
      return organizationCodes;
    }

    public void setOrganizationCodes(JsonArray organizationCodes)
    {
      this.organizationCodes = organizationCodes;
    }
  }

  public static class InviteUserBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject invite;

    @JsonDeserialize(using = JsonArrayDeserializer.class)
    JsonArray  roleIds;

    public JsonObject getInvite()
    {
      return invite;
    }

    public void setInvite(JsonObject invite)
    {
      this.invite = invite;
    }

    public JsonArray getRoleIds()
    {
      return roleIds;
    }

    public void setRoleIds(JsonArray roleIds)
    {
      this.roleIds = roleIds;
    }
  }

  public static class ApplyUserBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject account;

    @NotNull
    @JsonDeserialize(using = JsonArrayDeserializer.class)
    JsonArray  roleNames;

    public JsonObject getAccount()
    {
      return account;
    }

    public void setAccount(JsonObject account)
    {
      this.account = account;
    }

    public JsonArray getRoleNames()
    {
      return roleNames;
    }

    public void setRoleNames(JsonArray roleNames)
    {
      this.roleNames = roleNames;
    }

  }

  public static final String API_PATH = "registryaccount";

  @Autowired
  private AccountService     accountService;

  @GetMapping(API_PATH + "/page")
  public ResponseEntity<String> page(@RequestParam Integer pageNumber, @RequestParam Integer pageSize)
  {
    String json = this.accountService.page(this.getSessionId(), pageNumber, pageSize);

    return new ResponseEntity<String>(json, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-sras")
  public ResponseEntity<String> getSRAs(@RequestParam Integer pageNumber, @RequestParam Integer pageSize)
  {
    String json = this.accountService.getSRAs(this.getSessionId(), pageNumber, pageSize);

    return new ResponseEntity<String>(json, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/getRolesForUser")
  public ResponseEntity<String> getRolesForUser(@RequestParam String userOID)
  {
    RegistryRole[] roles = this.accountService.getRolesForUser(this.getSessionId(), userOID);

    JsonArray rolesJSONArray = this.createRoleMap(roles);

    return new ResponseEntity<String>(rolesJSONArray.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/edit")
  public ResponseEntity<String> edit(@Valid @RequestBody OidBody body)
  {
    JSONObject user = this.accountService.lock(this.getSessionId(), body.getOid());

    RegistryRole[] registryRoles = this.accountService.getRolesForUser(this.getSessionId(), body.getOid());

    JsonArray rolesJSONArray = this.createRoleMap(registryRoles);

    JsonObject response = new JsonObject();
    response.add("user", JsonParser.parseString(user.toString()));
    response.add("roles", rolesJSONArray);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> get()
  {
    JSONObject object = this.accountService.get(this.getSessionId());

    return new ResponseEntity<String>(object.toString(), HttpStatus.OK);
  }

  /**
   * Returns all roles associated with the given {@link OrganizationDTO} codes.
   * If no codes are provided then return all roles defined in the registry.
   * 
   * @param this.getClientRequest()
   * @param organizationCodes
   *          comma separated list of {@link OrganizationDTO} codes.
   * @return
   * @throws JSONException
   */
  @PostMapping(API_PATH + "/newInstance")
  public ResponseEntity<String> newInstance(@Valid @RequestBody OrgUserBody body)
  {

    String[] orgCodeArray = null;

    if (body.organizationCodes != null)
    {
      orgCodeArray = new String[body.organizationCodes.size()];
      for (int i = 0; i < body.organizationCodes.size(); i++)
      {
        orgCodeArray[i] = body.organizationCodes.get(i).getAsString();
      }
    }
    else
    {
      orgCodeArray = new String[0];
    }

    GeoprismUserDTO user = UserInviteDTO.newUserInst(this.getClientRequest());

    RegistryRole[] registryRoles = this.accountService.getRolesForOrganization(this.getSessionId(), orgCodeArray);
    JsonArray rolesJSONArray = this.createRoleMap(registryRoles);

    JSONObject response = new JSONObject();
    response.put("user", BusinessDTOToBasicJSON.getConverter(user, new NullConfiguration()).populate());
    response.put("roles", new JSONArray(rolesJSONArray.toString()));

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/newUserInstance")
  public ResponseEntity<String> newUserInstance()
  {
    GeoprismUserDTO user = UserInviteDTO.newUserInst(this.getClientRequest());

    JSONObject response = BusinessDTOToBasicJSON.getConverter(user, new NullConfiguration()).populate();

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody OidBody body)
  {
    accountService.remove(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(API_PATH + "/unlock")
  public ResponseEntity<Void> unlock(@Valid @RequestBody OidBody body)
  {
    this.accountService.unlock(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<String> apply(@Valid @RequestBody ApplyUserBody body)
  {
    String[] roleNameArray = null;

    if (body.roleNames != null)
    {      
      roleNameArray = new String[body.roleNames.size()];
      for (int i = 0; i < body.roleNames.size(); i++)
      {
        roleNameArray[i] = body.roleNames.get(i).getAsString();
      }
    }

    JSONObject user = this.accountService.apply(this.getSessionId(), body.account.toString(), roleNameArray);

    RegistryRole[] registryRoles = this.accountService.getRolesForUser(this.getSessionId(), user.getString(GeoprismUserDTO.OID));

    JsonArray rolesJSONArray = this.createRoleMap(registryRoles);

    JSONObject response = new JSONObject();
    response.put("user", user);
    response.put("roles", new JSONArray(rolesJSONArray.toString()));

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  /**
   * 
   * 
   * @param this.getClientRequest()
   * @param organizationCodes
   *          comma delimited list of registry codes. Returns all registry roles
   *          if empty.
   * @return
   * @throws JSONException
   */
  @PostMapping(API_PATH + "/newInvite")
  public ResponseEntity<String> newInvite(@Valid @RequestBody OrgUserBody body)
  {
    String[] orgCodeArray = null;

    if (body.organizationCodes != null)
    {
      orgCodeArray = new String[body.organizationCodes.size()];
      for (int i = 0; i < body.organizationCodes.size(); i++)
      {
        orgCodeArray[i] = body.organizationCodes.get(i).getAsString();
      }
    }
    else
    {
      orgCodeArray = new String[0];
    }

    JSONObject user = new JSONObject();
    user.put("newInstance", true);

    RegistryRole[] registryRoles = this.accountService.getRolesForOrganization(this.getSessionId(), orgCodeArray);
    JsonArray rolesJSONArray = this.createRoleMap(registryRoles);

    JSONObject response = new JSONObject();
    response.put("user", user);
    response.put("roles", new JSONArray(rolesJSONArray.toString()));

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
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
