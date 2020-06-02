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
  protected void enforceActorHasPermission(SingleActorDAOIF actor, ServerGeoObjectType got, Operation op, boolean allowRC)
  {
    if (!this.doesActorHavePermission(actor, got, op, allowRC))
    {
      Organization org = got.getOrganization();
      
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
  
  protected boolean doesActorHavePermission(SingleActorDAOIF actor, ServerGeoObjectType got, Operation op, boolean allowRC)
  {
    Organization thisOrg = got.getOrganization();
    
    if (thisOrg != null)
    {
      String thisOrgCode = thisOrg.getCode();
      
      Set<RoleDAOIF> roles = actor.authorizedRoles();
      
      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();
        
        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String orgCode = RegistryRole.Type.parseOrgCode(roleName);
          
          if (RegistryRole.Type.isRA_Role(roleName) && orgCode.equals(thisOrgCode))
          {
            return true;
          }
          else if ( ( (allowRC && RegistryRole.Type.isRC_Role(roleName)) || RegistryRole.Type.isRM_Role(roleName)) && orgCode.equals(thisOrgCode) )
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if (gotCode.equals(got.getCode()))
            {
              return true;
            }
          }
          else if ( (RegistryRole.Type.isAC_Role(roleName) || RegistryRole.Type.isRC_Role(roleName)) && op.equals(Operation.READ) && orgCode.equals(thisOrgCode))
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if (gotCode.equals(got.getCode()))
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
  public boolean canRead(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return this.doesActorHavePermission(actor, got, Operation.READ, true);
  }

  @Override
  public void enforceCanRead(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    this.enforceActorHasPermission(actor, got, Operation.READ, true);
  }

  @Override
  public boolean canWrite(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return this.doesActorHavePermission(actor, got, Operation.WRITE, false);
  }

  @Override
  public void enforceCanWrite(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    this.enforceActorHasPermission(actor, got, Operation.WRITE, false);
  }

  @Override
  public boolean canCreate(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return this.doesActorHavePermission(actor, got, Operation.CREATE, false);
  }

  @Override
  public void enforceCanCreate(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    this.enforceActorHasPermission(actor, got, Operation.CREATE, false);
  }

  @Override
  public boolean canWriteCR(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return this.doesActorHavePermission(actor, got, Operation.WRITE, true);
  }

  @Override
  public void enforceCanWriteCR(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    this.enforceActorHasPermission(actor, got, Operation.WRITE, true);
  }

  @Override
  public boolean canCreateCR(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    return this.doesActorHavePermission(actor, got, Operation.CREATE, true);
  }

  @Override
  public void enforceCanCreateCR(SingleActorDAOIF actor, ServerGeoObjectType got)
  {
    this.enforceActorHasPermission(actor, got, Operation.CREATE, true);
  }
}
