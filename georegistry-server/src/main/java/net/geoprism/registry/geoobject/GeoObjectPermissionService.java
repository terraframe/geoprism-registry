package net.geoprism.registry.geoobject;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.roles.DeleteGeoObjectPermissionException;
import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.roles.WriteGeoObjectPermissionException;

public class GeoObjectPermissionService implements GeoObjectPermissionServiceIF
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
  protected void enforceActorHasPermission(SingleActorDAOIF actor, String orgCode, String gotCode, Operation op, boolean allowRC)
  {
    if (!this.doesActorHavePermission(actor, orgCode, gotCode, op, allowRC))
    {
      Organization org = Organization.getByCode(orgCode);
      ServerGeoObjectType got = ServerGeoObjectType.get(gotCode);
      
      if (op.equals(Operation.WRITE))
      {
        WriteGeoObjectPermissionException ex = new WriteGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(got.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.READ))
      {
        ReadGeoObjectPermissionException ex = new ReadGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(got.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.DELETE))
      {
        DeleteGeoObjectPermissionException ex = new DeleteGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(got.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.CREATE))
      {
        CreateGeoObjectPermissionException ex = new CreateGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(got.getLabel().getValue());
        throw ex;
      }
    }
  }
  
  protected boolean doesActorHavePermission(SingleActorDAOIF actor, String orgCode, String gotCode, Operation op, boolean isChangeRequest)
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
          
          if (RegistryRole.Type.isRA_Role(roleName) && roleOrgCode.equals(orgCode))
          {
            return true;
          }
          else if ( ( (isChangeRequest && RegistryRole.Type.isRC_Role(roleName)) || RegistryRole.Type.isRM_Role(roleName)) && orgCode.equals(roleOrgCode) )
          {
            String roleGotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if (gotCode.equals(roleGotCode))
            {
              return true;
            }
          }
          else if ( (RegistryRole.Type.isAC_Role(roleName) || RegistryRole.Type.isRC_Role(roleName)) && op.equals(Operation.READ) && orgCode.equals(roleOrgCode))
          {
            String roleGotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if (gotCode.equals(roleGotCode))
            {
              return true;
            }
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
  public boolean canRead(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return this.doesActorHavePermission(actor, orgCode, gotCode, Operation.READ, true);
  }

  @Override
  public void enforceCanRead(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    this.enforceActorHasPermission(actor, orgCode, gotCode, Operation.READ, true);
  }

  @Override
  public boolean canWrite(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return this.doesActorHavePermission(actor, orgCode, gotCode, Operation.WRITE, false);
  }

  @Override
  public void enforceCanWrite(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    this.enforceActorHasPermission(actor, orgCode, gotCode, Operation.WRITE, false);
  }

  @Override
  public boolean canCreate(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return this.doesActorHavePermission(actor, orgCode, gotCode, Operation.CREATE, false);
  }

  @Override
  public void enforceCanCreate(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    this.enforceActorHasPermission(actor, orgCode, gotCode, Operation.CREATE, false);
  }

  @Override
  public boolean canWriteCR(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return this.doesActorHavePermission(actor, orgCode, gotCode, Operation.WRITE, true);
  }

  @Override
  public void enforceCanWriteCR(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    this.enforceActorHasPermission(actor, orgCode, gotCode, Operation.WRITE, true);
  }

  @Override
  public boolean canCreateCR(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    return this.doesActorHavePermission(actor, orgCode, gotCode, Operation.CREATE, true);
  }

  @Override
  public void enforceCanCreateCR(SingleActorDAOIF actor, String orgCode, String gotCode)
  {
    this.enforceActorHasPermission(actor, orgCode, gotCode, Operation.CREATE, true);
  }
}
