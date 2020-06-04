package net.geoprism.registry.geoobject;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.roles.GeoObjectAddChildPermissionException;
import net.geoprism.registry.roles.GeoObjectRemoveChildPermissionException;
import net.geoprism.registry.roles.GeoObjectViewRelationshipPermissionException;

public class GeoObjectRelationshipPermissionService implements GeoObjectRelationshipPermissionServiceIF
{
  protected void enforceActorHasPermission(SingleActorDAOIF actor, String orgCode, String parentTypeCode, String childTypeCode, Operation op, boolean isChangeRequest)
  {
    if (!this.doesActorHavePermission(actor, orgCode, parentTypeCode, childTypeCode, op, isChangeRequest))
    {
      Organization org = Organization.getByCode(orgCode);
      
      if (op.equals(Operation.ADD_CHILD))
      {
        GeoObjectAddChildPermissionException ex = new GeoObjectAddChildPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.DELETE_CHILD))
      {
        GeoObjectRemoveChildPermissionException ex = new GeoObjectRemoveChildPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.READ_CHILD))
      {
        GeoObjectViewRelationshipPermissionException ex = new GeoObjectViewRelationshipPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
    }
  }
  
  protected boolean doesActorHavePermission(SingleActorDAOIF actor, String orgCode, String parentTypeCode, String childTypeCode, Operation op, boolean isChangeRequest)
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
            return orgCode.equals(roleOrgCode) || op.equals(Operation.READ_CHILD);
          }
          else if ( RegistryRole.Type.isRM_Role(roleName) && orgCode.equals(roleOrgCode) )
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if ( parentTypeCode == null || childTypeCode == null
                || gotCode.equals(parentTypeCode) || gotCode.equals(childTypeCode))
            {
              return true;
            }
          }
          else if ( isChangeRequest && RegistryRole.Type.isRC_Role(roleName) && orgCode.equals(roleOrgCode) )
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if (gotCode.equals(parentTypeCode) || gotCode.equals(childTypeCode))
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
  public boolean canAddChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.ADD_CHILD, false);
  }

  @Override
  public void enforceCanAddChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.ADD_CHILD, false);
  }

  @Override
  public boolean canAddChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.ADD_CHILD, true);
  }

  @Override
  public void enforceCanAddChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.ADD_CHILD, true);
  }
  
  @Override
  public boolean canRemoveChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.DELETE_CHILD, false);
  }

  @Override
  public void enforceCanRemoveChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.DELETE_CHILD, false);
  }

  @Override
  public boolean canRemoveChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.DELETE_CHILD, true);
  }

  @Override
  public void enforceCanRemoveChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.DELETE_CHILD, true);
  }

  @Override
  public boolean canViewChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHavePermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.READ_CHILD, false);
  }

  @Override
  public void enforceCanViewChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasPermission(actor, orgCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, Operation.READ_CHILD, false);
  }
}
