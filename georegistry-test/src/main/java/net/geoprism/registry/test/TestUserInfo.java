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
package net.geoprism.registry.test;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;

public class TestUserInfo
{
  
  private String username;
  
  private String password;
  
  private String email;
  
  private String[] roleNameArray;
  
  public TestUserInfo(String username, String password, String email, String[] roleNameArray)
  {
    this.username = username;
    this.password = password;
    this.email = email;
    this.roleNameArray = roleNameArray == null ? new String[] {} : roleNameArray;
  }
  
  public void delete()
  {
    TestDataSet.deleteUser(username);
  }
  
  public GeoprismUser apply()
  {
    GeoprismUser user = this.getGeoprismUser();
    
    if (user != null)
    {
      return user;
    }
    else
    {
      return TestDataSet.createUser(username, password, email, roleNameArray);
    }
  }
  
  public GeoprismUser getGeoprismUser()
  {
    GeoprismUserQuery query = new GeoprismUserQuery(new QueryFactory());
    query.WHERE(query.getUsername().EQ(this.username));
    OIterator<? extends GeoprismUser> it = query.getIterator();
    
    try
    {
      if (it.hasNext())
      {
        return it.next();
      }
      else
      {
        return null;
      }
    }
    finally
    {
      it.close();
    }
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String[] getRoleNameArray()
  {
    return roleNameArray;
  }

  public void setRoleNameArray(String[] roleNameArray)
  {
    this.roleNameArray = roleNameArray;
  }
  
}
