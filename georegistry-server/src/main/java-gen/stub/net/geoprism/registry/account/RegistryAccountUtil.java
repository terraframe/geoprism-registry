package net.geoprism.registry.account;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.GeoprismProperties;
import net.geoprism.account.InvalidUserInviteToken;
import net.geoprism.account.UserInvite;
import net.geoprism.account.UserInviteQuery;
import net.geoprism.registry.service.AccountService;

public class RegistryAccountUtil extends RegistryAccountUtilBase
{
  private static final long serialVersionUID = 726983610;
  
  private static final Logger logger = LoggerFactory.getLogger(RegistryAccountUtil.class);
  
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
  public static void inviteComplete(java.lang.String token, net.geoprism.GeoprismUser user)
  {
    inviteCompleteInTrans(token, user);
  }

  @Transaction
  private static void inviteCompleteInTrans(java.lang.String token, net.geoprism.GeoprismUser user)
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

    if (invite.getRoleIds().length() > 0)
    {
      JSONArray array = new JSONArray(invite.getRoleIds());
      List<String> list = new LinkedList<String>();

      for (int i = 0; i < array.length(); i++)
      {
        list.add(array.getString(i));
      }

      new AccountService().applyInTransaction(user, list.toArray(new String[list.size()]), true);
    }
    else
    {
      user.apply();
    }

    invite.delete();

    logger.info("User [" + user.getUsername() + "] has been created via a user invite.");
  }
  
  public static net.geoprism.GeoprismUser newUserInst()
  {
    // This method not yet used, but provided in the hopes that it may be useful in the future.
    return null;
  }
}
