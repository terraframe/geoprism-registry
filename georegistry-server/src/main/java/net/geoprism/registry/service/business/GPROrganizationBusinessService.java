/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.stereotype.Repository;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.Roles;

import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.business.OrganizationBusinessService;
import net.geoprism.registry.business.OrganizationBusinessServiceIF;
import net.geoprism.registry.model.ServerOrganization;

@Repository
public class GPROrganizationBusinessService extends OrganizationBusinessService implements OrganizationBusinessServiceIF
{
  @Override
  @Transaction
  public ServerOrganization create(OrganizationDTO organizationDTO)
  {
    final ServerOrganization organization = super.create(organizationDTO);

    if (organization.getOrganization().isNew())
    {
      this.createRegistryAdminOrganizationRole(organization.getOrganization());
    }

    return organization;
  }

  /**
   * Creates a Registry Administrator {@link RoleDAOIF} for this
   * {@link Organization}.
   * 
   * Precondition: a {@link RoleDAOIF} does not exist for this
   * {@link Organization}. Precondition: the display label for the default
   * locale has a value for this {@link Organization}
   * 
   */
  private void createRegistryAdminOrganizationRole(Organization org)
  {
    String registryAdminRoleName = getRegistryAdminRoleName(org);

    String defaultDisplayLabel = org.getDisplayLabel().getDefaultValue() + " Registry Administrator";

    // Heads up: clean up move to Roles.java?
    Roles raOrgRole = new Roles();
    raOrgRole.setRoleName(registryAdminRoleName);
    raOrgRole.getDisplayLabel().setDefaultValue(defaultDisplayLabel);
    raOrgRole.apply();

    Roles orgRole = (Roles) org.getRole();
    RoleDAO orgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(orgRole);

    RoleDAO raOrgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(raOrgRole);
    orgRoleDAO.addInheritance(raOrgRoleDAO);

    // Inherit the permissions from the root RA role
    RoleDAO rootRA_DAO = (RoleDAO) BusinessFacade.getEntityDAO(Roles.findRoleByName(RegistryConstants.REGISTRY_ADMIN_ROLE));
    rootRA_DAO.addInheritance(raOrgRoleDAO);
  }

  /**
   * Returns the {@link RoleDAOIF} name for the Registry Administrator for this
   * {@link Organization}.
   * 
   * @return the {@link RoleDAOIF} name for the Registry Administrator for this
   *         {@link Organization}.
   */
  public String getRegistryAdminRoleName(Organization org)
  {
    return RegistryRole.Type.getRA_RoleName(org.getCode());
  }

  /**
   * Returns the Registry Administrator {@link Roles} for this
   * {@link Organization}.
   * 
   * @return the Registry Administrator {@link Roles} for this
   *         {@link Organization}.
   */
  public Roles getRegistryAdminRole(Organization org)
  {
    return Roles.findRoleByName(this.getRegistryAdminRoleName(org));
  }

  /**
   * Returns the Registry Administrator {@link Roles} for this
   * {@link Organization}.
   * 
   * @param organizationCode
   * 
   * @return the Registry Administrator {@link Roles} for this
   *         {@link Organization}.
   */
  public static Roles getRegistryAdminRole(String organizationCode)
  {
    return Roles.findRoleByName(RegistryRole.Type.getRA_RoleName(organizationCode));
  }

  @Override
  protected void deleteRoles(ServerOrganization sorg)
  {
    try
    {
      Roles raOrgRole = this.getRegistryAdminRole(sorg.getOrganization());
      raOrgRole.delete();
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
    }
  }

}
