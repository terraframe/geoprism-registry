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
