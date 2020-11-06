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
package net.geoprism.registry.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationUser;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.RegistryRoleConverter;
import net.geoprism.registry.model.ServerGeoObjectType;

public class AccountService
{
  private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

  public static AccountService getInstance()
  {
    return ServiceFactory.getAccountService();
  }

  @Request(RequestType.SESSION)
  public String page(String sessionId, Integer number) throws JSONException
  {
    return UserInfo.page(20, number).toString();
  }

  @Request(RequestType.SESSION)
  public JSONObject apply(String sessionId, String json, String[] roleNameArray)
  {
    JSONObject account = new JSONObject(json);

    return UserInfo.applyUserWithRoles(account, roleNameArray, false);
  }

  @Request(RequestType.SESSION)
  public JSONObject lock(String sessionId, String oid)
  {
    return UserInfo.lockByUser(oid);
  }

  @Request(RequestType.SESSION)
  public JSONObject get(String sessionId)
  {
    SingleActorDAOIF user = Session.getCurrentSession().getUser();

    return UserInfo.lockByUser(user.getOid());
  }

  @Request(RequestType.SESSION)
  public void unlock(String sessionId, String oid)
  {
    UserInfo.unlockByUser(oid);
  }

  /**
   * 
   * @param organizationCodes
   *          comma delimited list of registry codes. Returns all registry roles
   *          if empty.
   * 
   * @return all of the roles are set to assigned equals false
   */
  @Request(RequestType.SESSION)
  public RegistryRole[] getRolesForUser(String sessionId, String userOID)
  {
    GeoprismUser geoPrismUser = GeoprismUser.get(userOID);

    List<RegistryRole> registryRoles = new LinkedList<RegistryRole>();

    Set<String> roleNameSet = new HashSet<String>();

    OIterator<? extends com.runwaysdk.system.Roles> roleIterator = geoPrismUser.getAllAssignedRole();

    for (Roles role : roleIterator)
    {
      RegistryRole registryRole = new RegistryRoleConverter().build(role);

      if (registryRole != null)
      {
        registryRole.setAssigned(true);

        LocalizedValueConverter.populateOrganizationDisplayLabel(registryRole);
        LocalizedValueConverter.populateGeoObjectTypeLabel(registryRole);

        registryRoles.add(registryRole);
        roleNameSet.add(registryRole.getName());
      }
    }

    // Add the registry roles that the user can be a member of based on their
    // organization affiliation
    OIterator<? extends Business> organizationIterators = geoPrismUser.getParents(OrganizationUser.CLASS);

    for (Business business : organizationIterators)
    {
      Organization organization = (Organization) business;
      List<RegistryRole> orgRoleIterator = this.getRolesForOrganization(organization.getCode());

      for (RegistryRole registryRole : orgRoleIterator)
      {
        if (!roleNameSet.contains(registryRole.getName()))
        {
          registryRoles.add(registryRole);
        }
      }
    }

    if (!roleNameSet.contains(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE))
    {
      Roles sra = Roles.findRoleByName(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);
      RegistryRole rrSRA = new RegistryRoleConverter().build(sra);
      rrSRA.setAssigned(false);

      registryRoles.add(rrSRA);
    }

    return registryRoles.stream().sorted(Comparator.comparing(RegistryRole::getOrganizationCode).thenComparing(RegistryRole::getGeoObjectTypeCode)).toArray(size -> new RegistryRole[size]);
  }

  /**
   * 
   * @param organizationCodes
   *          comma delimited list of registry codes. Returns all registry roles
   *          if empty.
   * 
   * @return all of the roles are set to assigned equals false
   */
  @Request(RequestType.SESSION)
  public RegistryRole[] getRolesForOrganization(String sessionId, String[] organizationCodes)
  {
    List<RegistryRole> registryRoles = this.getRolesForOrganization(organizationCodes);

    return registryRoles.stream().sorted(Comparator.comparing(RegistryRole::getOrganizationCode).thenComparing(RegistryRole::getGeoObjectTypeCode)).toArray(size -> new RegistryRole[size]);
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    UserInfo.removeByUser(oid);
  }

  private List<RegistryRole> getRolesForOrganization(String... organizationCodes)
  {
    List<RegistryRole> registryRoleList = new LinkedList<RegistryRole>();

    List<String> orgCodesToProcess = new LinkedList<String>();

    for (String organizationCode : organizationCodes)
    {
      if (!organizationCode.trim().equals(""))
      {
        orgCodesToProcess.add(organizationCode.trim());
      }
    }

    List<Organization> organizationList;

    if (orgCodesToProcess.size() == 0)
    {
      // Add the SRA role
      String sraRoleName = RegistryRole.Type.getSRA_RoleName();
      Roles sraRole = Roles.findRoleByName(sraRoleName);
      registryRoleList.add(new RegistryRoleConverter().build(sraRole));

      organizationList = Organization.getOrganizationsFromCache();
    }
    else
    {
      organizationList = new LinkedList<Organization>();

      for (String organizationCode : organizationCodes)
      {
        organizationList.add(Organization.getByCode(organizationCode));
      }
    }

    for (Organization organization : organizationList)
    {
      this.addRolesForOrganization(registryRoleList, organization);
    }

    return registryRoleList;
  }

  private void addRolesForOrganization(List<RegistryRole> registryRoleList, Organization organization)
  {
    LocalizedValue orgDisplayLabel = LocalizedValueConverter.convert(organization.getDisplayLabel());

    // Add the RA role
    Roles adminRole = organization.getRegistryAdminiRole();
    RegistryRole adminRegistryRole = new RegistryRoleConverter().build(adminRole);
    adminRegistryRole.setOrganizationLabel(orgDisplayLabel);
    registryRoleList.add(adminRegistryRole);

    Map<String, ServerGeoObjectType> geoObjectTypeInfo = organization.getGeoObjectTypes();

    for (String typeCode : geoObjectTypeInfo.keySet())
    {
      ServerGeoObjectType type = geoObjectTypeInfo.get(typeCode);

      // Permissions are assigned and validated against the super type
      // The cannot be assigned directly to the child type.
      if (type.getSuperType() == null)
      {
        // Add the RM role
        String rmRoleName = RegistryRole.Type.getRM_RoleName(organization.getCode(), typeCode);
        Roles rmRole = Roles.findRoleByName(rmRoleName);
        RegistryRole rmRegistryRole = new RegistryRoleConverter().build(rmRole);
        rmRegistryRole.setOrganizationLabel(orgDisplayLabel);
        LocalizedValue label = type.getLabel();
        rmRegistryRole.setGeoObjectTypeLabel(label);
        registryRoleList.add(rmRegistryRole);

        // Add the RC role
        String rcRoleName = RegistryRole.Type.getRC_RoleName(organization.getCode(), typeCode);
        Roles rcRole = Roles.findRoleByName(rcRoleName);
        RegistryRole rcRegistryRole = new RegistryRoleConverter().build(rcRole);
        rcRegistryRole.setOrganizationLabel(orgDisplayLabel);
        rcRegistryRole.setGeoObjectTypeLabel(label);
        registryRoleList.add(rcRegistryRole);

        // Add the AC role
        String acRoleName = RegistryRole.Type.getAC_RoleName(organization.getCode(), typeCode);
        Roles acRole = Roles.findRoleByName(acRoleName);
        RegistryRole acRegistryRole = new RegistryRoleConverter().build(acRole);
        acRegistryRole.setOrganizationLabel(orgDisplayLabel);
        acRegistryRole.setGeoObjectTypeLabel(label);
        registryRoleList.add(acRegistryRole);
      }
    }
  }
}
