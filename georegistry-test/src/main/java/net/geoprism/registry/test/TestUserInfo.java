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
    this.roleNameArray = roleNameArray;
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
