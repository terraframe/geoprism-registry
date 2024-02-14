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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.Roles;

import net.geoprism.registry.ObjectHasDataException;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.view.Page;

@Service
@Primary
public class GPROrganizationBusinessService extends OrganizationBusinessService implements OrganizationBusinessServiceIF
{
  @Autowired
  private BusinessTypeBusinessServiceIF service;

  @Override
  public void apply(ServerOrganization organization, ServerOrganization parent)
  {
    boolean isNew = organization.getOrganization().isNew();

    super.apply(organization, parent);

    if (isNew)
    {
      this.createRegistryAdminOrganizationRole(organization.getOrganization());
    }
  }

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

  @Override
  @Transaction
  public void delete(ServerOrganization sorg)
  {
    this.permissionService.enforceActorCanDelete();

    // Can't delete if there's existing data
    List<ServerHierarchyType> hierarchyTypes = ServiceFactory.getMetadataCache().getAllHierarchyTypes();

    for (ServerHierarchyType ht : hierarchyTypes)
    {
      if (ht.getOrganizationCode().equals(sorg.getCode()))
      {
        throw new ObjectHasDataException();
      }
    }

    this.deleteRoles(sorg);

    sorg.delete();
  }

  @Override
  public ServerOrganization update(OrganizationDTO organizationDTO)
  {
    // Ensure that all children are enabled
    ServerOrganization org = ServerOrganization.getByCode(organizationDTO.getCode());

    if (organizationDTO.getEnabled() != null && !organizationDTO.getEnabled())
    {
      // Can't disable if there's existing data
      ServiceFactory.getMetadataCache().getAllHierarchyTypes().forEach(type -> {
        if (type.getOrganizationCode().equals(organizationDTO.getCode()))
        {
          throw new ObjectHasDataException();
        }
      });

      ServiceFactory.getMetadataCache().getAllGeoObjectTypes().forEach(type -> {
        if (type.getOrganizationCode().equals(organizationDTO.getCode()))
        {
          throw new ObjectHasDataException();
        }
      });

      if (this.service.getForOrganization(org).size() > 0)
      {
        throw new ObjectHasDataException();
      }

      // Can't disable if a child is still enabled
      Page<ServerOrganization> page = this.getChildren(org, null, null);
      page.getResults().forEach(child -> {
        if (child.getEnabled())
        {
          throw new ObjectHasDataException();
        }
      });
    }
    else
    {
      // Ensure that the parent is enabled if there is a parent
      ServerOrganization parent = org.getParent();

      if (parent != null && !parent.getEnabled())
      {
        throw new ObjectHasDataException();
      }
    }

    return super.update(organizationDTO);
  }

  @Override
  public void move(ServerOrganization organization, ServerOrganization newParent)
  {
    // Orgs must be enabled
    if (!newParent.getEnabled() || !organization.getEnabled())
    {
      // TODO: Localized exception
      throw new ObjectHasDataException();
    }

    super.move(organization, newParent);
  }

  /**
   * Return a map of {@link GeoObjectType} codes and labels for this
   * {@link Organization}.
   * 
   * @return a map of {@link GeoObjectType} codes and labels for this
   *         {@link Organization}.
   */
  public Map<String, ServerGeoObjectType> getGeoObjectTypes(ServerOrganization organization)
  {
    return ServerGeoObjectType.getAll().stream().filter(type -> {
      return type.getOrganizationCode().equals(organization.getCode());
    }).collect(Collectors.toMap(t -> t.getCode(), t -> t));
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
      super.deleteRoles(sorg);

      Roles raOrgRole = this.getRegistryAdminRole(sorg.getOrganization());
      raOrgRole.delete();
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
    }
  }

}
