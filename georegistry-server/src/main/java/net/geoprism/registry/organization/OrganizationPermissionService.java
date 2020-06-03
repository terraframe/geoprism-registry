package net.geoprism.registry.organization;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationRAException;

public class OrganizationPermissionService
{
  /**
   * Returns true if the provided actor has permission to this organization.
   */
  public boolean doesActorHavePermission(SingleActorDAOIF actor, Organization org)
  {
    if (actor == null) // null actor is assumed to be SYSTEM
    {
      return true;
    }
    
    Set<RoleDAOIF> roles = actor.authorizedRoles();

    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
      {
        String orgCode = RegistryRole.Type.parseOrgCode(roleName);

        if (orgCode.equals(org.getCode()))
        {
          return true;
        }
      }
      else if (RegistryRole.Type.isSRA_Role(roleName))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * Throws an exception if the provided actor does not have permissions to this
   * organization. Uses {{Organization.doesActorHavePermission}} to check
   * permissions.
   * 
   * @param actor
   */
  public void enforceActorHasPermission(SingleActorDAOIF actor, Organization org)
  {
    if (!this.doesActorHavePermission(actor, org))
    {
      OrganizationRAException ex = new OrganizationRAException();
      throw ex;
    }
  }
}
