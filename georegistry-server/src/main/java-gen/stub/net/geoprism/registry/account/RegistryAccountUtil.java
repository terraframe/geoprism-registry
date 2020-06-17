/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.account;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.GeoprismProperties;
import net.geoprism.GeoprismUser;
import net.geoprism.account.InvalidUserInviteToken;
import net.geoprism.account.UserInvite;
import net.geoprism.account.UserInviteQuery;
import net.geoprism.registry.UserInfo;

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

    if ( ( System.currentTimeMillis() - invite.getStartTime().getTime() ) > ( GeoprismProperties.getInviteUserTokenExpireTime() * 3600000 ))
    {
      throw new InvalidUserInviteToken();
    }

    JSONObject account = new JSONObject(json);

    if (invite.getRoleIds().length() > 0)
    {
      JSONArray array = new JSONArray(invite.getRoleIds());
      List<String> list = new LinkedList<String>();

      for (int i = 0; i < array.length(); i++)
      {
        list.add(array.getString(i));
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
