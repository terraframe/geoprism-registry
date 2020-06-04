package net.geoprism.registry.organization;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationRAException;
import net.geoprism.registry.SRAException;

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
  
  protected boolean isActorSRA(SingleActorDAOIF actor)
  {
    if (actor == null)
    {
      return true;
    }
    
    Set<RoleDAOIF> roles = Session.getCurrentSession().getUser().authorizedRoles();
    
    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();
      
      if (RegistryRole.Type.isSRA_Role(roleName))
      {
        return true;
      }
    }
    
    return false;
  }
  
  protected void enforceActorSRA(SingleActorDAOIF actor)
  {
    if (!isActorSRA(actor))
    {
      SRAException ex = new SRAException();
      throw ex;
    }
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

  public void enforceActorCanCreate(SingleActorDAOIF user)
  {
    enforceActorSRA(user);
  }

  public void enforceActorCanUpdate(SingleActorDAOIF user)
  {
    enforceActorSRA(user);
  }
  
  public boolean canActorCreate(SingleActorDAOIF user)
  {
    return isActorSRA(user);
  }

  public boolean canActorUpdate(SingleActorDAOIF user)
  {
    return isActorSRA(user);
  }

  public void enforceActorCanDelete(SingleActorDAOIF user)
  {
    enforceActorSRA(user);
  }
  
  public boolean canActorDelete(SingleActorDAOIF user)
  {
    return isActorSRA(user);
  }
}
