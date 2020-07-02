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
package net.geoprism.registry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.business.rbac.UserDAOIF;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.attributes.AttributeValueException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;

import net.geoprism.ConfigurationIF;
import net.geoprism.ConfigurationService;
import net.geoprism.DefaultConfiguration;
import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;
import net.geoprism.registry.conversion.RegistryRoleConverter;
import net.geoprism.registry.service.ServiceFactory;

public class UserInfo extends UserInfoBase
{
  private static final long serialVersionUID = 1228611031;

  public UserInfo()
  {
    super();
  }

  @Override
  @Transaction
  public void apply()
  {
    this.setOwner(this.getGeoprismUser());

    super.apply();
  }

  public static JSONObject page(Integer pageSize, Integer pageNumber)
  {
    List<Organization> organizations = Organization.getUserAdminOrganizations();
    boolean isSRA = ServiceFactory.getRolePermissionService().isSRA(Session.getCurrentSession().getUser());

    if (organizations.size() > 0 || isSRA)
    {
      ValueQuery vQuery = new ValueQuery(new QueryFactory());

      GeoprismUserQuery uQuery = new GeoprismUserQuery(vQuery);
      UserInfoQuery iQuery = new UserInfoQuery(vQuery);

      vQuery.SELECT(uQuery.getOid(), uQuery.getUsername(), uQuery.getFirstName(), uQuery.getLastName(), uQuery.getPhoneNumber(), uQuery.getEmail());
      vQuery.SELECT(iQuery.getAltFirstName(), iQuery.getAltLastName(), iQuery.getAltPhoneNumber(), iQuery.getPosition());

      vQuery.WHERE(new LeftJoinEq(uQuery.getOid(), iQuery.getGeoprismUser()));

      if (organizations.size() > 0)
      {
        // restrict by org code
        OrganizationQuery orgQuery = new OrganizationQuery(vQuery);
        OrganizationUserQuery relQuery = new OrganizationUserQuery(vQuery);

        for (Organization org : organizations)
        {
          orgQuery.OR(orgQuery.getCode().EQ(org.getCode()));
        }

        vQuery.WHERE(relQuery.parentOid().EQ(orgQuery.getOid()));
        vQuery.WHERE(uQuery.getOid().EQ(relQuery.childOid()));
      }

      vQuery.ORDER_BY_ASC(uQuery.getUsername());

      JSONArray results = new JSONArray();

      OIterator<ValueObject> it = vQuery.getIterator(pageSize, pageNumber);

      try
      {
        while (it.hasNext())
        {
          ValueObject vObject = it.next();

          JSONObject result = new JSONObject();
          result.put(GeoprismUser.OID, vObject.getValue(GeoprismUser.OID));
          result.put(GeoprismUser.USERNAME, vObject.getValue(GeoprismUser.USERNAME));
          result.put(GeoprismUser.FIRSTNAME, vObject.getValue(GeoprismUser.FIRSTNAME));
          result.put(GeoprismUser.LASTNAME, vObject.getValue(GeoprismUser.LASTNAME));
          result.put(GeoprismUser.PHONENUMBER, vObject.getValue(GeoprismUser.PHONENUMBER));
          result.put(GeoprismUser.EMAIL, vObject.getValue(GeoprismUser.EMAIL));
          result.put(UserInfo.ALTFIRSTNAME, vObject.getValue(UserInfo.ALTFIRSTNAME));
          result.put(UserInfo.ALTLASTNAME, vObject.getValue(UserInfo.ALTLASTNAME));
          result.put(UserInfo.ALTPHONENUMBER, vObject.getValue(UserInfo.ALTPHONENUMBER));
          result.put(UserInfo.POSITION, vObject.getValue(UserInfo.POSITION));

          results.put(result);
        }
      }
      finally
      {
        it.close();
      }

      JSONObject page = new JSONObject();
      page.put("resultSet", results);
      page.put("count", vQuery.getCount());
      page.put("pageNumber", pageNumber);
      page.put("pageSize", pageSize);

      return page;
    }

    JSONObject page = new JSONObject();
    page.put("resultSet", new JSONArray());
    page.put("count", 0);
    page.put("pageNumber", pageNumber);
    page.put("pageSize", pageSize);

    return page;
  }

  @Transaction
  public static JSONObject lockByUser(String userId)
  {
    GeoprismUser user = GeoprismUser.lock(userId);

    UserInfo info = UserInfo.getByUser(user);

    if (info != null)
    {
      info.lock();
    }

    return UserInfo.serialize(user, info);
  }

  @Transaction
  public static void unlockByUser(String userId)
  {
    GeoprismUser user = GeoprismUser.unlock(userId);

    UserInfo info = UserInfo.getByUser(user);

    if (info != null)
    {
      info.unlock();
    }
  }

  @Transaction
  public static void removeByUser(String userId)
  {
    GeoprismUser user = GeoprismUser.get(userId);

    UserInfo info = UserInfo.getByUser(user);

    if (info != null)
    {
      info.delete();
    }

    user.delete();
  }

  @Transaction
  public static JSONObject applyUserWithRoles(JSONObject account, String[] roleNameArray, boolean isUserInvite)
  {
    GeoprismUser geoprismUser = deserialize(account);

    if (roleNameArray != null && roleNameArray.length == 0)
    {
      // TODO : Better Error
      throw new AttributeValueException("You're attempting to apply a user with zero roles?", "");
    }

    /*
     * Make sure they have permissions to all these new roles they want to
     * assign
     */
    if (!isUserInvite && Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      Set<RoleDAOIF> myRoles = Session.getCurrentSession().getUser().authorizedRoles();

      boolean hasSRA = false;
      for (RoleDAOIF myRole : myRoles)
      {
        if (RegistryRole.Type.isSRA_Role(myRole.getRoleName()))
        {
          hasSRA = true;
        }
      }

      if (!hasSRA && roleNameArray != null)
      {
        for (String roleName : roleNameArray)
        {
          boolean hasPermission = false;

          if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
          {
            String orgCodeArg = RegistryRole.Type.parseOrgCode(roleName);

            for (RoleDAOIF myRole : myRoles)
            {
              if (RegistryRole.Type.isRA_Role(myRole.getRoleName()))
              {
                String myOrgCode = RegistryRole.Type.parseOrgCode(myRole.getRoleName());

                if (myOrgCode.equals(orgCodeArg))
                {
                  hasPermission = true;
                  break;
                }
              }
            }
          }
          else if (RegistryRole.Type.isSRA_Role(roleName))
          {
            SRAException ex = new SRAException();
            throw ex;
          }
          else
          {
            hasPermission = true;
          }

          if (!hasPermission)
          {
            OrganizationRAException ex = new OrganizationRAException();
            throw ex;
          }
        }
      }
    }

    // They're not allowed to change the admin username
    if (!geoprismUser.isNew())
    {
      GeoprismUser adminUser = getAdminUser();
      if (adminUser != null && adminUser.getOid().equals(geoprismUser.getOid()) && !geoprismUser.getUsername().equals(RegistryConstants.ADMIN_USER_NAME))
      {
        // TODO : Better Error
        throw new AttributeValueException("You can't change the admin username", RegistryConstants.ADMIN_USER_NAME);
      }
    }

    geoprismUser.apply();

    if (roleNameArray != null)
    {
      List<Roles> newRoles = new LinkedList<Roles>();

      Set<String> roleIdSet = new HashSet<String>();
      for (String roleName : roleNameArray)
      {
        Roles role = Roles.findRoleByName(roleName);

        roleIdSet.add(role.getOid());
        newRoles.add(role);
      }

      List<ConfigurationIF> configurations = ConfigurationService.getConfigurations();

      for (ConfigurationIF configuration : configurations)
      {
        configuration.configureUserRoles(roleIdSet);
      }

      UserDAOIF user = UserDAO.get(geoprismUser.getOid());

      // Remove existing roles.
      Set<RoleDAOIF> userRoles = user.assignedRoles();
      for (RoleDAOIF roleDAOIF : userRoles)
      {
        RoleDAO roleDAO = RoleDAO.get(roleDAOIF.getOid()).getBusinessDAO();

        if (! ( geoprismUser.getUsername().equals(RegistryConstants.ADMIN_USER_NAME) && ( roleDAO.getRoleName().equals(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE) || roleDAO.getRoleName().equals(DefaultConfiguration.ADMIN) ) ))
        {
          roleDAO.deassignMember(user);
        }
      }

      // Delete existing relationships with Organizations.
      QueryFactory qf = new QueryFactory();
      OrganizationUserQuery q = new OrganizationUserQuery(qf);
      q.WHERE(q.childOid().EQ(geoprismUser.getOid()));

      OIterator<? extends OrganizationUser> i = q.getIterator();
      i.forEach(r -> r.delete());

      /*
       * Assign roles and associate with the user
       */
      Set<String> organizationSet = new HashSet<String>();
      for (Roles role : newRoles)
      {
        RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);
        roleDAO.assignMember(user);

        RegistryRole registryRole = new RegistryRoleConverter().build(role);
        if (registryRole != null)
        {
          String organizationCode = registryRole.getOrganizationCode();

          if (organizationCode != null && !organizationCode.equals("") && !organizationSet.contains(organizationCode))
          {
            Organization organization = Organization.getByCode(organizationCode);
            organization.addUsers(geoprismUser).apply();
            organizationSet.add(organizationCode);
          }
        }
      }
    }

    UserInfo info = getByUser(geoprismUser);

    if (info == null)
    {
      info = new UserInfo();
      info.setGeoprismUser(geoprismUser);
    }
    else
    {
      info.lock();
    }

    if (account.has(UserInfo.ALTFIRSTNAME))
    {
      info.setAltFirstName(account.get(UserInfo.ALTFIRSTNAME).toString());
    }
    else
    {
      info.setAltFirstName("");
    }

    if (account.has(UserInfo.ALTLASTNAME))
    {
      info.setAltLastName(account.get(UserInfo.ALTLASTNAME).toString());
    }
    else
    {
      info.setAltLastName("");
    }

    if (account.has(UserInfo.ALTPHONENUMBER))
    {
      info.setAltPhoneNumber(account.get(UserInfo.ALTPHONENUMBER).toString());
    }
    else
    {
      info.setAltPhoneNumber("");
    }

    if (account.has(UserInfo.POSITION))
    {
      info.setPosition(account.get(UserInfo.POSITION).toString());
    }
    else
    {
      info.setPosition("");
    }

    if (account.has(UserInfo.DEPARTMENT))
    {
      info.setDepartment(account.get(UserInfo.DEPARTMENT).toString());
    }
    else
    {
      info.setDepartment("");
    }

    info.apply();

    return serialize(geoprismUser, info);
  }

  private static JSONObject serialize(GeoprismUser user, UserInfo info)
  {
    JSONObject result = new JSONObject();
    result.put(GeoprismUser.OID, user.getOid());
    result.put(GeoprismUser.USERNAME, user.getUsername());
    result.put(GeoprismUser.FIRSTNAME, user.getFirstName());
    result.put(GeoprismUser.LASTNAME, user.getLastName());
    result.put(GeoprismUser.PHONENUMBER, user.getPhoneNumber());
    result.put(GeoprismUser.EMAIL, user.getEmail());
    result.put(GeoprismUser.INACTIVE, user.getInactive());

    if (info != null)
    {
      result.put(UserInfo.ALTFIRSTNAME, info.getAltFirstName());
      result.put(UserInfo.ALTLASTNAME, info.getAltLastName());
      result.put(UserInfo.ALTPHONENUMBER, info.getAltPhoneNumber());
      result.put(UserInfo.POSITION, info.getPosition());
      result.put(UserInfo.DEPARTMENT, info.getDepartment());
    }

    result.put("newInstance", user.isNew());

    return result;
  }

  public static GeoprismUser deserialize(JSONObject account)
  {
    GeoprismUser user = null;

    if (account.has(GeoprismUser.OID))
    {
      String userId = account.getString(GeoprismUser.OID);

      user = GeoprismUser.get(userId);
    }
    else
    {
      user = new GeoprismUser();
    }

    user.setUsername(account.getString(GeoprismUser.USERNAME));
    user.setFirstName(account.getString(GeoprismUser.FIRSTNAME));
    user.setLastName(account.getString(GeoprismUser.LASTNAME));
    user.setEmail(account.getString(GeoprismUser.EMAIL));

    if (account.has(GeoprismUser.PHONENUMBER))
    {
      user.setPhoneNumber(account.getString(GeoprismUser.PHONENUMBER));
    }

    if (account.has(GeoprismUser.INACTIVE))
    {
      user.setInactive(account.getBoolean(GeoprismUser.INACTIVE));
    }

    if (account.has(GeoprismUser.PASSWORD))
    {
      String password = account.getString(GeoprismUser.PASSWORD);

      if (password != null && password.length() > 0)
      {
        user.setPassword(password);
      }
    }

    return user;
  }

  public static UserInfo getByUser(GeoprismUser user)
  {
    if (user.isAppliedToDB())
    {
      UserInfoQuery query = new UserInfoQuery(new QueryFactory());
      query.WHERE(query.getGeoprismUser().EQ(user));

      try (OIterator<? extends UserInfo> it = query.getIterator())
      {

        if (it.hasNext())
        {
          return it.next();
        }
      }
    }

    return null;
  }

  private static GeoprismUser getAdminUser()
  {
    GeoprismUserQuery guq = new GeoprismUserQuery(new QueryFactory());
    guq.WHERE(guq.getUsername().EQ(RegistryConstants.ADMIN_USER_NAME));
    OIterator<? extends GeoprismUser> it = guq.getIterator();

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

}
