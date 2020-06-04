package net.geoprism.registry.service;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationRAException;
import net.geoprism.registry.SRAException;
import net.geoprism.registry.roles.RAException;

public class RolePermissionService
{
  public boolean isSRA(SingleActorDAOIF actor)
  {
    if (actor == null)
    {
      return true;
    }
    
    Set<RoleDAOIF> roles = actor.authorizedRoles();
    
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
  
  public void enforceSRA(SingleActorDAOIF actor)
  {
    if (!isSRA(actor))
    {
      SRAException ex = new SRAException();
      throw ex;
    }
  }
  
  public boolean isRA(SingleActorDAOIF actor)
  {
    return isRA(actor, null);
  }
  
  public boolean isRA(SingleActorDAOIF actor, String orgCode)
  {
    if (actor == null)
    {
      return true;
    }
    
    Set<RoleDAOIF> roles = actor.authorizedRoles();
    
    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();
      
      if (RegistryRole.Type.isRA_Role(roleName))
      {
        String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
        
        if (orgCode != null)
        {
          return orgCode.equals(roleOrgCode);
        }
        else
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
  
  public void enforceRA(SingleActorDAOIF actor)
  {
    enforceRA(actor, null);
  }
  
  public void enforceRA(SingleActorDAOIF actor, String orgCode)
  {
    if (!isRA(actor, orgCode))
    {
      if (orgCode != null)
      {
        Organization org = Organization.getByCode(orgCode);
        
        OrganizationRAException ex = new OrganizationRAException();
        ex.setOrganizationLabel(org.getDisplayLabel().getValue());
        throw ex;
      }
      else
      {
        RAException ex = new RAException();
        throw ex;
      }
    }
  }
}
