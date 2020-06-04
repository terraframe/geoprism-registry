package net.geoprism.registry.geoobjecttype;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.roles.CreateGeoObjectTypePermissionException;
import net.geoprism.registry.roles.DeleteGeoObjectTypePermissionException;
import net.geoprism.registry.roles.ReadGeoObjectTypePermissionException;
import net.geoprism.registry.roles.WriteGeoObjectTypePermissionException;

public class GeoObjectTypePermissionService implements GeoObjectTypePermissionServiceIF
{
  /**
   * Operation must be one of:
   * - WRITE (Update)
   * - READ
   * - DELETE
   * - CREATE
   * 
   * @param actor
   * @param op
   */
  protected void enforceActorHasPermission(SingleActorDAOIF actor, String orgCode, String gotLabel, Operation op)
  {
    if (!this.doesActorHavePermission(actor, orgCode, op))
    {
      Organization org = Organization.getByCode(orgCode);
      
      if (op.equals(Operation.WRITE))
      {
        WriteGeoObjectTypePermissionException ex = new WriteGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(gotLabel);
        throw ex;
      }
      else if (op.equals(Operation.READ))
      {
        ReadGeoObjectTypePermissionException ex = new ReadGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(gotLabel);
        throw ex;
      }
      else if (op.equals(Operation.DELETE))
      {
        DeleteGeoObjectTypePermissionException ex = new DeleteGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(gotLabel);
        throw ex;
      }
      else if (op.equals(Operation.CREATE))
      {
        CreateGeoObjectTypePermissionException ex = new CreateGeoObjectTypePermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
    }
  }
  
  protected boolean doesActorHavePermission(SingleActorDAOIF actor, String orgCode, Operation op)
  {
    if (actor == null) // null actor is assumed to be SYSTEM
    {
      return true;
    }
    
    if (orgCode != null)
    {
      Set<RoleDAOIF> roles = actor.authorizedRoles();
      
      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();
        
        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
          
          if (RegistryRole.Type.isRA_Role(roleName))
          {
            return orgCode.equals(roleOrgCode) || op.equals(Operation.READ);
          }
          else if ( op.equals(Operation.READ) && orgCode.equals(roleOrgCode) && (RegistryRole.Type.isAC_Role(roleName) || RegistryRole.Type.isRC_Role(roleName) || RegistryRole.Type.isRM_Role(roleName)))
          {
            return true;
          }
        }
        else if (RegistryRole.Type.isSRA_Role(roleName))
        {
          return true;
        }
      }
    }
    
    return false;
  }

  @Override
  public boolean canRead(SingleActorDAOIF actor, String orgCode)
  {
    return this.doesActorHavePermission(actor, orgCode, Operation.READ);
  }

  @Override
  public void enforceCanRead(SingleActorDAOIF actor, String orgCode, String gotLabel)
  {
    this.enforceActorHasPermission(actor, orgCode, gotLabel, Operation.READ);
  }

  @Override
  public boolean canWrite(SingleActorDAOIF actor, String orgCode)
  {
    return this.doesActorHavePermission(actor, orgCode, Operation.WRITE);
  }

  @Override
  public void enforceCanWrite(SingleActorDAOIF actor, String orgCode, String gotLabel)
  {
    this.enforceActorHasPermission(actor, orgCode, gotLabel, Operation.WRITE);
  }

  @Override
  public boolean canCreate(SingleActorDAOIF actor, String orgCode)
  {
    return this.doesActorHavePermission(actor, orgCode, Operation.CREATE);
  }

  @Override
  public void enforceCanCreate(SingleActorDAOIF actor, String orgCode)
  {
    this.enforceActorHasPermission(actor, orgCode, null, Operation.CREATE);
  }
  
  @Override
  public boolean canDelete(SingleActorDAOIF actor, String orgCode)
  {
    return this.doesActorHavePermission(actor, orgCode, Operation.DELETE);
  }

  @Override
  public void enforceCanDelete(SingleActorDAOIF actor, String orgCode, String gotLabel)
  {
    this.enforceActorHasPermission(actor, orgCode, gotLabel, Operation.DELETE);
  }
  
}
