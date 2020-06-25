package com.runwaysdk.build.domain;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

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
  }

}
