package net.geoprism.registry.controller;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.AccountController;
import net.geoprism.GeoprismUserDTO;
import net.geoprism.account.UserInviteDTO;
import net.geoprism.registry.service.AccountService;

import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ParseType;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.request.ServletRequestIF;

@Controller(url = "registryaccount")
public class RegistryAccountController
{
  private AccountService    accountService;

  
  public RegistryAccountController()
  {
    this.accountService = AccountService.getInstance();
  }
  
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF page(ClientRequestIF request, @RequestParamter(name = "number") Integer number) throws JSONException
  {
    return new AccountController().page(request, number);
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    return new AccountController().edit(request, oid) ;
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF get(ClientRequestIF request) throws JSONException
  {
    return new AccountController().get(request);
  }
  
  /**
   * Returns all roles associated with the given {@link OrganizationDTO} codes. If no codes are provided then
   * return all roles defined in the registry.
   * 
   * @param request
   * @param organizationCodes comma separated list of {@link OrganizationDTO} codes. 
   * @return
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newInstance(ClientRequestIF request, @RequestParamter(name = "organizationCodes") String organizationCodes) throws JSONException
  {
    String[] orgCodeArray = null;
    
    if (organizationCodes != null)
    {
      orgCodeArray = organizationCodes.split("\\,");
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
    response.set("roles", rolesJSONArray);
    
    return response;
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

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newUserInstance(ClientRequestIF request) throws JSONException
  { 
    return new AccountController().newUserInstance(request);
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    return new AccountController().remove(request, oid);
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    return new AccountController().unlock(request, oid);
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "account", parser = ParseType.BASIC_JSON) GeoprismUserDTO account, @RequestParamter(name = "roleIds") String roleIds) throws JSONException
  {
    List<String> list = new LinkedList<String>();
    
    if (roleIds != null)
    {
      JSONArray array = new JSONArray(roleIds);

      for (int i = 0; i < array.length(); i++)
      {
        list.add(array.getString(i));
      }
    }
    
    String[] roleIdArray = list.toArray((new String[list.size()]));
       
    GeoprismUserDTO returnAccount = this.accountService.apply(request.getSessionId(), account, roleIdArray);

    return new RestBodyResponse(returnAccount);
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF inviteUser(ClientRequestIF request, ServletRequestIF sr, @RequestParamter(name = "invite") String sInvite, @RequestParamter(name = "roleIds") String roleIds) throws JSONException
  {
    return new AccountController().inviteUser(request, sr, sInvite, roleIds);
  }
 
  
  /**
   * 
   * 
   * @param request
   * @param organizationCodes comma delimited list of registry codes. Returns all registry roles if empty.
   * @return
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF newInvite(ClientRequestIF request) throws JSONException
  {        
    return new AccountController().newInvite(request) ;
  }
  
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF inviteComplete(ClientRequestIF request, @RequestParamter(name = "user", parser = ParseType.BASIC_JSON) GeoprismUserDTO user, @RequestParamter(name = "token") String token) throws JSONException
  {
    return new AccountController().inviteComplete(request, user, token);
  }
}
