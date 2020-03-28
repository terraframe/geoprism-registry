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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.geoprism.ConfigurationIF;
import net.geoprism.ConfigurationService;
import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserDTO;
import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.RegistryRoleConverter;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.business.rbac.UserDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.facade.FacadeUtil;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.Roles;

public class AccountService
{
  public static AccountService getInstance()
  {
    return ServiceFactory.getAccountService();
  }

  @Request(RequestType.SESSION)
  public GeoprismUserDTO apply(String sessionId, GeoprismUserDTO account, String[] roleIds)
  {    
    GeoprismUser geoprismUser = (GeoprismUser)FacadeUtil.populateMutable(sessionId, account);
    
    geoprismUser = this.applyInTransaction(geoprismUser, roleIds);
    
    GeoprismUserDTO returnAccount = (GeoprismUserDTO)FacadeUtil.convertTypeToDTO(sessionId, geoprismUser);
    
    return returnAccount;
  }
  
  @Transaction
  public GeoprismUser applyInTransaction(GeoprismUser geoprismUser, String[] roleIds)
  {
    geoprismUser.apply();

    Set<String> set = new HashSet<String>(Arrays.asList(roleIds));

    List<ConfigurationIF> configurations = ConfigurationService.getConfigurations();

    for (ConfigurationIF configuration : configurations)
    {
      configuration.configureUserRoles(set);
    }

    UserDAOIF user = UserDAO.get(geoprismUser.getOid());

    // Remove existing roles.
    Set<RoleDAOIF> userRoles = user.assignedRoles();
    for (RoleDAOIF roleDAOIF : userRoles)
    {
      RoleDAO roleDAO = RoleDAO.get(roleDAOIF.getOid()).getBusinessDAO();
      roleDAO.deassignMember(user);
    }

    /*
     * Assign roles
     */
    for (String roleId : set)
    {
      RoleDAO roleDAO = RoleDAO.get(roleId).getBusinessDAO();
      roleDAO.assignMember(user);
    }
    
    return geoprismUser;
  }
  
  /**
   * 
   * @param organizationCodes comma delimited list of registry codes. Returns all registry roles if empty.
   * 
   * @return all of the roles are set to assigned equals false
   */
  @Request(RequestType.SESSION)
  public RegistryRole[] getRolesForOrganization(String sessionId, String[] organizationCodes)
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
      addRolesForOrganization(registryRoleList, organization);
    }

    
    return registryRoleList.toArray(new RegistryRole[registryRoleList.size()]);
  }
  
  private void addRolesForOrganization(List<RegistryRole> registryRoleList, Organization organization)
  {
    Roles adminRole = organization.getRegistryAdminiRole();
   
    registryRoleList.add(new RegistryRoleConverter().build(adminRole));
   
    String[] geoRegistryTypes = organization.getGeoObjectTypes();
   
    for (String typeCode : geoRegistryTypes)
    {
      // Add the RM role
      String rmRoleName = RegistryRole.Type.getRM_RoleName(organization.getCode(), typeCode);
      Roles rmRole = Roles.findRoleByName(rmRoleName);
      registryRoleList.add(new RegistryRoleConverter().build(rmRole));
    
      // Add the RC role
      String rcRoleName = RegistryRole.Type.getRC_RoleName(organization.getCode(), typeCode);
      Roles rcRole = Roles.findRoleByName(rcRoleName);
      registryRoleList.add(new RegistryRoleConverter().build(rcRole));

      // Add the AC role
      String acRoleName = RegistryRole.Type.getAC_RoleName(organization.getCode(), typeCode);
      Roles acRole = Roles.findRoleByName(acRoleName);
      registryRoleList.add(new RegistryRoleConverter().build(acRole));
    }
  }   
  
}
