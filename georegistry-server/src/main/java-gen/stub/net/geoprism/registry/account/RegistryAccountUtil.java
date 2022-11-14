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
package net.geoprism.registry.account;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.attributes.AttributeValueException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.Roles;

import net.geoprism.EmailSetting;
import net.geoprism.GeoprismProperties;
import net.geoprism.GeoprismUser;
import net.geoprism.account.InvalidUserInviteToken;
import net.geoprism.account.UserInvite;
import net.geoprism.account.UserInviteQuery;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.Organization;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.conversion.RegistryRoleConverter;

public class RegistryAccountUtil extends RegistryAccountUtilBase
{
  private static final long   serialVersionUID = 726983610;

  private static final Logger logger           = LoggerFactory.getLogger(RegistryAccountUtil.class);

  public RegistryAccountUtil()
  {
    super();
  }
  
  /**
   * MdMethod
   * 
   * Initiates a user invite request. If the user already has one in progress,
   * it will be invalidated and a new one will be issued. If the server's email
   * settings have not been properly set up, or the user does not exist, an
   * error will be thrown.
   * 
   * @param username
   */
  @Authenticate
  public static void initiate(String invite, String roleIds, String serverExternalUrl)
  {
    initiateInTrans(invite, roleIds);
  }

  @Transaction
  public static void initiateInTrans(String sInvite, String roleIds)
  {
    if (roleIds == null || roleIds.length() == 0 || JsonParser.parseString(roleIds).getAsJsonArray().size() == 0)
    {
      // TODO : Better Error
      throw new AttributeValueException("You're attempting to invite a user with zero roles?", "");
    }
    
    JSONObject joInvite = new JSONObject(sInvite);

    String email = joInvite.getString("email");

    UserInvite invite = new UserInvite();
    invite.setEmail(email);

    UserInviteQuery query = new UserInviteQuery(new QueryFactory());
    query.WHERE(query.getEmail().EQi(invite.getEmail()));
    OIterator<? extends UserInvite> it = query.getIterator();

    while (it.hasNext())
    {
      it.next().delete();
    }

    invite.setStartTime(new Date());
    invite.setToken(generateEncryptedToken(invite.getEmail()));
    invite.setRoleIds(roleIds);

    invite.apply();

    RegistryAccountUtil.sendEmail(invite, roleIds);
  }
  
  public static void sendEmail(UserInvite invite, String roleIds)
  {
    final String serverExternalUrl = GeoregistryProperties.getRemoteServerUrl();
    
    String address = invite.getEmail();
    String link = serverExternalUrl + "cgr/manage#/admin/invite-complete/" + invite.getToken();

    String subject = LocalizationFacade.localize("user.invite.email.subject");
    
    String body = LocalizationFacade.localize("user.invite.email.body");
    body = body.replaceAll("\\\\n", "\n");
    body = body.replace("${link}", link);
    body = body.replace("${expireTime}", getLocalizedExpireTime());
    
    JsonArray roleNameArray = JsonParser.parseString(roleIds).getAsJsonArray();
    
    String orgLabel = "??";
    Set<String> roleLabels = new HashSet<String>();
    
    for (int i = 0; i < roleNameArray.size(); ++i)
    {
      String roleName = roleNameArray.get(i).getAsString();
      
      Roles role = Roles.findRoleByName(roleName);
      
      RegistryRole registryRole = new RegistryRoleConverter().build(role);
      
      if (orgLabel.equals("??"))
      {
        String orgCode = registryRole.getOrganizationCode();
        if (orgCode != null && orgCode.length() > 0)
        {
          orgLabel = Organization.getByCode(orgCode).getDisplayLabel().getValue().trim();
        }
      }
      
      String roleLabel;
      if (RegistryRole.Type.isRA_Role(roleName))
      {
        roleLabel = Roles.findRoleByName("cgr.RegistryAdministrator").getDisplayLabel().getValue().trim();
      }
      else
      {
        roleLabel = role.getDisplayLabel().getValue().trim();
      }
      
      roleLabels.add(roleLabel);
    }
    
    body = body.replace("${roles}", StringUtils.join(roleLabels, ", "));
    body = body.replace("${organization}", orgLabel);

    EmailSetting.sendEmail(subject, body, new String[] { address });
  }
  
  private static String getLocalizedExpireTime()
  {
    final int expireTimeInHours = GeoprismProperties.getInviteUserTokenExpireTime();
    
    final String hours = LocalizationFacade.localize("user.invite.email.hours");
    final String days = LocalizationFacade.localize("user.invite.email.days");
    final String hoursAndDays = LocalizationFacade.localize("user.invite.email.hoursAndDays");
    
    if (expireTimeInHours < 24)
    {
      return hours.replace("${hours}", String.valueOf(expireTimeInHours));
    }
    else if (expireTimeInHours % 24 == 0)
    {
      return days.replace("${days}", String.valueOf(expireTimeInHours/24));
    }
    else
    {
      return hoursAndDays.replace("${days}", String.valueOf(expireTimeInHours/24)).replace("${hours}", String.valueOf(expireTimeInHours - (24 * (expireTimeInHours/24))));
    }
  }
  
  public static String generateEncryptedToken(String email)
  {
    String hashedTime = UUID.nameUUIDFromBytes(String.valueOf(System.currentTimeMillis()).getBytes()).toString();

    String hashedEmail = UUID.nameUUIDFromBytes(email.getBytes()).toString();

    return hashedTime + hashedEmail;
  }

  /**
   * MdMethod
   * 
   * Completes the user invite request by verifying the token is valid, creating
   * the requested user, and then invalidating the request.
   * 
   * @see {{net.geoprism.account.UserInvite.complete}} (in geoprism)
   * 
   * @param token
   */
  @Authenticate
  public static void inviteComplete(java.lang.String token, String user)
  {
    inviteCompleteInTrans(token, user);
  }

  @Transaction
  private static void inviteCompleteInTrans(java.lang.String token, String json)
  {
    UserInviteQuery query = new UserInviteQuery(new QueryFactory());
    query.WHERE(query.getToken().EQ(token));
    OIterator<? extends UserInvite> reqIt = query.getIterator();

    UserInvite invite;
    if (reqIt.hasNext())
    {
      invite = reqIt.next();
      invite.appLock();
    }
    else
    {
      throw new InvalidUserInviteToken();
    }

    if ( ( System.currentTimeMillis() - invite.getStartTime().getTime() ) > ( GeoprismProperties.getInviteUserTokenExpireTime() * 3600000L ))
    {
      throw new InvalidUserInviteToken();
    }

    JsonObject account = JsonParser.parseString(json).getAsJsonObject();

    if (invite.getRoleIds().length() > 0)
    {
      JsonArray array = JsonParser.parseString(invite.getRoleIds()).getAsJsonArray();
      List<String> list = new LinkedList<String>();

      for (int i = 0; i < array.size(); i++)
      {
        list.add(array.get(i).getAsString());
      }

      UserInfo.applyUserWithRoles(account, list.toArray(new String[list.size()]), true);
    }
    else
    {
      UserInfo.applyUserWithRoles(account, new String[] {}, true);
    }

    invite.delete();

    logger.info("User [" + account.get(GeoprismUser.USERNAME) + "] has been created via a user invite.");
  }

  public static net.geoprism.GeoprismUser newUserInst()
  {
    // This method not yet used, but provided in the hopes that it may be useful
    // in the future.
    return null;
  }
}
