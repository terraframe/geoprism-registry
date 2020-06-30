package com.runwaysdk.build.domain;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.UserInfoQuery;

public class UserInfoPatch
{
  public static void main(String[] args)
  {
    new UserInfoPatch().doIt();
  }

  @Transaction
  private void doIt()
  {
    UserInfoQuery query = new UserInfoQuery(new QueryFactory());

    try (OIterator<? extends UserInfo> it = query.getIterator())
    {
      while (it.hasNext())
      {
        UserInfo info = it.next();
        info.appLock();
        info.apply();
      }
    }

    // Ensure all geoprism users have a user info
    GeoprismUserQuery gQuery = new GeoprismUserQuery(new QueryFactory());

    try (OIterator<? extends GeoprismUser> it = gQuery.getIterator())
    {
      while (it.hasNext())
      {
        GeoprismUser user = it.next();

        UserInfo info = UserInfo.getByUser(user);

        if (info == null)
        {
          info = new UserInfo();
          info.setGeoprismUser(user);
          info.apply();
        }

      }
    }

  }

}
