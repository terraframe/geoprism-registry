package net.geoprism.registry.service;

import org.springframework.stereotype.Component;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.metadata.graph.MdGeoVertex;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.registry.InvalidMasterListCodeException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.RegistryConstants;

@Component
public class ServerClassificationObjectService implements ClassificationObjectServiceIF
{

  @Override
  public void assignPermissions(ComponentIF component)
  {
    Roles sraRole = Roles.findRoleByName(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);

    this.assignPermissions(component, sraRole, true);

    Roles raRole = Roles.findRoleByName(RegistryConstants.REGISTRY_ADMIN_ROLE);

    this.assignPermissions(component, raRole, false);

    Roles rmRole = Roles.findRoleByName(RegistryConstants.REGISTRY_MAINTAINER_ROLE);

    this.assignPermissions(component, rmRole, false);

    Roles rcRole = Roles.findRoleByName(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);

    this.assignPermissions(component, rcRole, false);

    Roles acRole = Roles.findRoleByName(RegistryConstants.API_CONSUMER_ROLE);

    this.assignPermissions(component, acRole, false);
  }

  /**
   * Assigns all permissions to the {@link ComponentIF} to the given role.
   * 
   * Precondition: component is either a {@link MdGeoVertex} or a
   * {@link MdBusiness}.
   * 
   * @param component
   * @param role
   * @param includeWrite
   *          TODO
   */
  private void assignPermissions(ComponentIF component, Roles role, boolean includeWrite)
  {
    RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);
    roleDAO.grantPermission(Operation.READ, component.getOid());
    roleDAO.grantPermission(Operation.READ_ALL, component.getOid());

    if (includeWrite)
    {
      roleDAO.grantPermission(Operation.CREATE, component.getOid());
      roleDAO.grantPermission(Operation.DELETE, component.getOid());
      roleDAO.grantPermission(Operation.WRITE, component.getOid());

      roleDAO.grantPermission(Operation.WRITE_ALL, component.getOid());
    }
  }

  @Override
  public void validateName(String name)
  {
    if (!MasterList.isValidName(name))
    {
      throw new InvalidMasterListCodeException("The geo object type code has an invalid character");
    }
  }

}
