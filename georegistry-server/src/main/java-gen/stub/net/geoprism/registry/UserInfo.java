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
package net.geoprism.registry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.business.rbac.UserDAOIF;
import com.runwaysdk.dataaccess.AttributeBooleanIF;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.attributes.AttributeValueException;
import com.runwaysdk.dataaccess.attributes.entity.AttributeBoolean;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;

import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;
import net.geoprism.account.RoleConstants;
import net.geoprism.registry.conversion.RegistryRoleConverter;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.service.ServiceFactory;

public class UserInfo extends UserInfoBase
{
  private static final long   serialVersionUID = 1228611031;

  private static final Random RANDOM           = new Random();

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
    final RolePermissionService perms = ServiceFactory.getRolePermissionService();

    List<Organization> organizations = Organization.getUserOrganizations();

    boolean isSRA = perms.isSRA();
    boolean isRMorRCorAC = ( !isSRA && !perms.isRA() ) && ( perms.isRM() || perms.isRC() || perms.isAC() );

    List<ExternalSystem> externalSystemList = ExternalSystem.getExternalSystemsForOrg(1, 100);
    JSONArray externalSystems = new JSONArray();
    for (ExternalSystem externalSystem : externalSystemList)
    {
      externalSystems.put(new JSONObject(externalSystem.toJSON().toString()));
    }

    if (organizations.size() > 0 || isSRA)
    {
      ValueQuery vQuery = new ValueQuery(new QueryFactory());

      GeoprismUserQuery uQuery = new GeoprismUserQuery(vQuery);
      UserInfoQuery iQuery = new UserInfoQuery(vQuery);

      vQuery.SELECT(uQuery.getOid(), uQuery.getUsername(), uQuery.getFirstName(), uQuery.getLastName(), uQuery.getPhoneNumber(), uQuery.getEmail(), uQuery.getInactive());
      vQuery.SELECT(iQuery.getAltFirstName(), iQuery.getAltLastName(), iQuery.getAltPhoneNumber(), iQuery.getPosition());
      vQuery.SELECT(iQuery.getExternalSystemOid());

      vQuery.WHERE(new LeftJoinEq(uQuery.getOid(), iQuery.getGeoprismUser()));

      if (organizations.size() > 0)
      {
        // restrict by org code
        OrganizationQuery orgQuery = new OrganizationQuery(vQuery);
        OrganizationUserQuery relQuery = new OrganizationUserQuery(vQuery);

        vQuery.WHERE(new LeftJoinEq(uQuery.getOid(), relQuery.getChild()));
        vQuery.WHERE(new LeftJoinEq(relQuery.getParent(), orgQuery.getOid()));

        Condition cond = null;

        for (Organization org : organizations)
        {
          if (cond == null)
          {
            cond = orgQuery.getCode().EQ(org.getCode());
          }
          else
          {
            cond = cond.OR(orgQuery.getCode().EQ(org.getCode()));
          }
        }

        cond = cond.OR(orgQuery.getCode().EQ((String) null));

        vQuery.AND(cond);
      }

      if (isRMorRCorAC)
      {
        vQuery.WHERE(uQuery.getInactive().EQ(false));
      }

      vQuery.ORDER_BY_ASC(uQuery.getUsername());

      return serializePage(pageSize, pageNumber, externalSystems, vQuery);
    }

    JSONObject page = new JSONObject();
    page.put("resultSet", new JSONArray());
    page.put("count", 0);
    page.put("pageNumber", pageNumber);
    page.put("pageSize", pageSize);
    page.put("externalSystems", externalSystems);

    return page;
  }

  private static JSONObject serializePage(Integer pageSize, Integer pageNumber, JSONArray externalSystems, ValueQuery vQuery)
  {
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
        result.put(GeoprismUser.INACTIVE, AttributeBoolean.getBooleanValue((AttributeBooleanIF) vObject.getAttributeIF(GeoprismUser.INACTIVE)));
        result.put(UserInfo.ALTFIRSTNAME, vObject.getValue(UserInfo.ALTFIRSTNAME));
        result.put(UserInfo.ALTLASTNAME, vObject.getValue(UserInfo.ALTLASTNAME));
        result.put(UserInfo.ALTPHONENUMBER, vObject.getValue(UserInfo.ALTPHONENUMBER));
        result.put(UserInfo.POSITION, vObject.getValue(UserInfo.POSITION));
        result.put(UserInfo.EXTERNALSYSTEMOID, vObject.getValue(UserInfo.EXTERNALSYSTEMOID));

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
    page.put("externalSystems", externalSystems);

    return page;
  }

  public static JSONObject getSRAs(Integer pageSize, Integer pageNumber)
  {
    RoleDAOIF role = RoleDAO.findRole(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
    Set<SingleActorDAOIF> actors = role.assignedActors();
    Set<String> oids = actors.parallelStream().map(actor -> actor.getOid()).collect(Collectors.toSet());

    ValueQuery vQuery = new ValueQuery(new QueryFactory());

    GeoprismUserQuery uQuery = new GeoprismUserQuery(vQuery);
    UserInfoQuery iQuery = new UserInfoQuery(vQuery);

    vQuery.SELECT(uQuery.getOid(), uQuery.getUsername(), uQuery.getFirstName(), uQuery.getLastName(), uQuery.getPhoneNumber(), uQuery.getEmail(), uQuery.getInactive());
    vQuery.SELECT(iQuery.getAltFirstName(), iQuery.getAltLastName(), iQuery.getAltPhoneNumber(), iQuery.getPosition());
    vQuery.SELECT(iQuery.getExternalSystemOid());

    vQuery.WHERE(new LeftJoinEq(uQuery.getOid(), iQuery.getGeoprismUser()));
    vQuery.AND(uQuery.getOid().IN(oids.toArray(new String[oids.size()])));

    vQuery.ORDER_BY_ASC(uQuery.getUsername());

    return serializePage(pageSize, pageNumber, new JSONArray(), vQuery);
  }

  @Transaction
  public static JSONObject getByUser(String userId)
  {
    GeoprismUser user = GeoprismUser.get(userId);

    UserInfo info = UserInfo.getByUser(user);

    return UserInfo.serialize(user, info);
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
  public static JSONObject applyUserWithRoles(JsonObject account, String[] roleNameArray, boolean isUserInvite)
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

//        ConfigurationIF.configureUserRoles(roleIdSet);
      RoleDAOIF admin = RoleDAO.findRole(RoleConstants.ADMIN);
      RoleDAOIF builder = RoleDAO.findRole(RoleConstants.DASHBOARD_BUILDER);

      if (! ( roleIdSet.contains(admin.getOid()) || roleIdSet.contains(builder.getOid()) ))
      {
        RoleDAOIF role = RoleDAO.findRole(RoleConstants.DECISION_MAKER);

        roleIdSet.add(role.getOid());
      }
      

      UserDAOIF user = UserDAO.get(geoprismUser.getOid());

      // Remove existing roles.
      Set<RoleDAOIF> userRoles = user.assignedRoles();
      for (RoleDAOIF roleDAOIF : userRoles)
      {
        RoleDAO roleDAO = RoleDAO.get(roleDAOIF.getOid()).getBusinessDAO();

        if (! ( geoprismUser.getUsername().equals(RegistryConstants.ADMIN_USER_NAME) && ( roleDAO.getRoleName().equals(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE) || roleDAO.getRoleName().equals(RoleConstants.ADMIN) ) ))
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
      info.setAltFirstName(account.get(UserInfo.ALTFIRSTNAME).getAsString());
    }
    else
    {
      info.setAltFirstName("");
    }

    if (account.has(UserInfo.ALTLASTNAME))
    {
      info.setAltLastName(account.get(UserInfo.ALTLASTNAME).getAsString());
    }
    else
    {
      info.setAltLastName("");
    }

    if (account.has(UserInfo.ALTPHONENUMBER))
    {
      info.setAltPhoneNumber(account.get(UserInfo.ALTPHONENUMBER).getAsString());
    }
    else
    {
      info.setAltPhoneNumber("");
    }

    if (account.has(UserInfo.POSITION))
    {
      info.setPosition(account.get(UserInfo.POSITION).getAsString());
    }
    else
    {
      info.setPosition("");
    }

    if (account.has(UserInfo.DEPARTMENT))
    {
      info.setDepartment(account.get(UserInfo.DEPARTMENT).getAsString());
    }
    else
    {
      info.setDepartment("");
    }

    if (account.has(UserInfo.EXTERNALSYSTEMOID))
    {
      info.setExternalSystemOid(account.get(UserInfo.EXTERNALSYSTEMOID).getAsString());
    }
    else
    {
      info.setExternalSystemOid("");
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
      result.put(UserInfo.EXTERNALSYSTEMOID, info.getExternalSystemOid());
    }

    result.put("newInstance", user.isNew());

    return result;
  }

  public static GeoprismUser deserialize(JsonObject account)
  {
    GeoprismUser user = null;

    if (account.has(GeoprismUser.OID))
    {
      String userId = account.get(GeoprismUser.OID).getAsString();

      user = GeoprismUser.get(userId);
    }
    else
    {
      user = new GeoprismUser();
    }

    user.setUsername(account.get(GeoprismUser.USERNAME).getAsString());
    user.setFirstName(account.get(GeoprismUser.FIRSTNAME).getAsString());
    user.setLastName(account.get(GeoprismUser.LASTNAME).getAsString());
    user.setEmail(account.get(GeoprismUser.EMAIL).getAsString());

    if (account.has(GeoprismUser.PHONENUMBER) && account.get(GeoprismUser.PHONENUMBER).getAsString().length() > 0)
    {
      user.setPhoneNumber(account.get(GeoprismUser.PHONENUMBER).getAsString());
    }

    if (account.has(GeoprismUser.INACTIVE))
    {
      user.setInactive(account.get(GeoprismUser.INACTIVE).getAsBoolean());
    }

    if (account.has(GeoprismUser.PASSWORD) && account.get(GeoprismUser.PASSWORD).getAsString().length() > 0)
    {
      String password = account.get(GeoprismUser.PASSWORD).getAsString();

      if (password != null && password.length() > 0)
      {
        user.setPassword(password);
      }
    }
    else if (account.has(UserInfo.EXTERNALSYSTEMOID) && account.get(UserInfo.EXTERNALSYSTEMOID).getAsString().length() > 0)
    {
      user.setPassword(String.valueOf(RANDOM.nextLong()));
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
