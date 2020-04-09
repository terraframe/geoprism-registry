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
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessDTO;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.business.rbac.UserDAOIF;
import com.runwaysdk.dataaccess.DuplicateGraphPathException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.facade.FacadeUtil;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.transport.conversion.ConversionFacade;

import net.geoprism.ConfigurationIF;
import net.geoprism.ConfigurationService;
import net.geoprism.DefaultConfiguration;
import net.geoprism.GeoprismProperties;
import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserDTO;
import net.geoprism.GeoprismUserQuery;
import net.geoprism.account.GeoprismUserViewQuery;
import net.geoprism.account.InvalidUserInviteToken;
import net.geoprism.account.UserInvite;
import net.geoprism.account.UserInviteQuery;
import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.OrganizationUser;
import net.geoprism.registry.OrganizationUserQuery;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.RegistryRoleConverter;

public class AccountService
{
  private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
  
  public static AccountService getInstance()
  {
    return ServiceFactory.getAccountService();
  }
  
  @Request(RequestType.SESSION)
  public String page(String sessionId, String[] organizationCodes, Integer number) throws JSONException
  {
    QueryFactory qf = new QueryFactory();
    
    GeoprismUserViewQuery userViewQuery = new GeoprismUserViewQuery(qf);

    List<Condition> conditions = new LinkedList<Condition>();
    if (organizationCodes.length > 0)
    {
      // restrict by org code
      OrganizationQuery orgQuery = new OrganizationQuery(qf);
      for (String orgCode : organizationCodes)
      {
        conditions.add(orgQuery.getCode().EQ(orgCode));
        
        orgQuery.OR(orgQuery.getCode().EQ(orgCode));
      }
      
      OrganizationUserQuery relQuery = new OrganizationUserQuery(qf);
      relQuery.WHERE(relQuery.parentOid().EQ(orgQuery.getOid()));

            
      GeoprismUserQuery uq = new GeoprismUserQuery(qf);
      uq.WHERE(uq.getOid().EQ(relQuery.childOid()));
      
      userViewQuery.WHERE(userViewQuery.getUsername().EQ(uq.getUsername()));
    }
   
    
    userViewQuery.restrictRows(10, number);
    userViewQuery.ORDER_BY_ASC(userViewQuery.getUsername());
    
    String json =  userViewQuery.toJSON().toString();
    
    return json;
  }
  
  @Request(RequestType.SESSION)
  public GeoprismUserDTO apply(String sessionId, GeoprismUserDTO geoprismUserDTO, String[] roleNameArray)
  {    
    GeoprismUser geoprismUser = convertToBusiness(sessionId, geoprismUserDTO);

    geoprismUser = this.applyInTransaction(geoprismUser, roleNameArray);  

    geoprismUserDTO = convertToDTO(sessionId, geoprismUserDTO, geoprismUser);

    return geoprismUserDTO;
  }

  @Transaction
  public GeoprismUser applyInTransaction(GeoprismUser geoprismUser, String[] roleNameArray)
  {
    geoprismUser.apply();
    
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
      
      // Don't remove things like the root admin role
      if (!excludedRole(roleDAOIF.getRoleName()))
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
      RoleDAO roleDAO = (RoleDAO)BusinessFacade.getEntityDAO(role);
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
    
    geoprismUser.apply();
    
    return geoprismUser;
  }
  
  /**
   * 
   * @param organizationCodes comma delimited list of registry codes. Returns all registry roles if empty.
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
    
    // Add the registry roles that the user can be a member of based on their organization affiliation 
    OIterator<? extends Business> organizationIterators = geoPrismUser.getParents(OrganizationUser.CLASS);
    
    for (Business business : organizationIterators)
    {
      Organization organization = (Organization)business;
      List<RegistryRole> orgRoleIterator = this.getRolesForOrganization(organization.getCode());
      
      for (RegistryRole registryRole : orgRoleIterator)
      {
        if (!roleNameSet.contains(registryRole.getName()))
        {
          registryRoles.add(registryRole);
        }
      }
    }

    return registryRoles.stream().sorted(Comparator.comparing(RegistryRole::getOrganizationCode).thenComparing(RegistryRole::getGeoObjectTypeCode)).toArray(size -> new RegistryRole[size]);
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
    List<RegistryRole> registryRoles = this.getRolesForOrganization(organizationCodes);
    
    return registryRoles.stream().sorted(Comparator.comparing(RegistryRole::getOrganizationCode).thenComparing(RegistryRole::getGeoObjectTypeCode)).toArray(size -> new RegistryRole[size]);
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
    
    Map<String, LocalizedValue> geoObjectTypeInfo = organization.getGeoObjectTypes();
   
    for (String typeCode : geoObjectTypeInfo.keySet())
    {
      LocalizedValue geoObjectTypeDisplayLabel = geoObjectTypeInfo.get(typeCode);

      // Add the RM role
      String rmRoleName = RegistryRole.Type.getRM_RoleName(organization.getCode(), typeCode);
      Roles rmRole = Roles.findRoleByName(rmRoleName);
      RegistryRole rmRegistryRole = new RegistryRoleConverter().build(rmRole);
      rmRegistryRole.setOrganizationLabel(orgDisplayLabel);
      rmRegistryRole.setGeoObjectTypeLabel(geoObjectTypeDisplayLabel);
      registryRoleList.add(rmRegistryRole);
    
      // Add the RC role
      String rcRoleName = RegistryRole.Type.getRC_RoleName(organization.getCode(), typeCode);
      Roles rcRole = Roles.findRoleByName(rcRoleName);
      RegistryRole rcRegistryRole = new RegistryRoleConverter().build(rcRole);
      rcRegistryRole.setOrganizationLabel(orgDisplayLabel);
      rcRegistryRole.setGeoObjectTypeLabel(geoObjectTypeDisplayLabel);
      registryRoleList.add(rcRegistryRole);

      // Add the AC role
      String acRoleName = RegistryRole.Type.getAC_RoleName(organization.getCode(), typeCode);
      Roles acRole = Roles.findRoleByName(acRoleName);
      RegistryRole acRegistryRole = new RegistryRoleConverter().build(acRole);
      acRegistryRole.setOrganizationLabel(orgDisplayLabel);
      acRegistryRole.setGeoObjectTypeLabel(geoObjectTypeDisplayLabel);
      registryRoleList.add(acRegistryRole);
    }
  }   
  
  
  private boolean excludedRole(String roleName)
  {    
    if (roleName.equals(DefaultConfiguration.ADMIN))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  private GeoprismUser convertToBusiness(String sessionId, GeoprismUserDTO geoprismUserDTO)
  {
    BusinessDTO genericBusinessDTO = (BusinessDTO) ConversionFacade.createGenericCopy(geoprismUserDTO);
    GeoprismUser geoprismUser = (GeoprismUser)FacadeUtil.populateMutable(sessionId, genericBusinessDTO);
    return geoprismUser;
  }
  
  private GeoprismUserDTO convertToDTO(String sessionId, GeoprismUserDTO geoprismUserDTO, GeoprismUser geoprismUser)
  {
    BusinessDTO genericBusinessDTO = (BusinessDTO)FacadeUtil.populateComponentDTOIF(sessionId, geoprismUser, true);
    ConversionFacade.typeSafeCopy(geoprismUserDTO.getRequest(), genericBusinessDTO, geoprismUserDTO);
    
    return geoprismUserDTO; 
  }
  
}
