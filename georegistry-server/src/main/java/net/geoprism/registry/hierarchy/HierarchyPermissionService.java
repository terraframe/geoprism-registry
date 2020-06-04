package net.geoprism.registry.hierarchy;

import java.util.Set;

import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.LocatedIn;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.roles.CreateHierarchyPermissionException;
import net.geoprism.registry.roles.DeleteHierarchyPermissionException;
import net.geoprism.registry.roles.HierarchyRelationshipPermissionException;
import net.geoprism.registry.roles.ReadHierarchyPermissionException;
import net.geoprism.registry.roles.UpdateHierarchyPermissionException;

public class HierarchyPermissionService implements HierarchyPermissionServiceIF
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
  protected void enforceActorHasPermission(SingleActorDAOIF actor, Organization org, Operation op)
  {
    if (!doesActorHavePermission(actor, org, op))
    {
      if (op.equals(Operation.WRITE))
      {
        UpdateHierarchyPermissionException ex = new UpdateHierarchyPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.READ))
      {
        ReadHierarchyPermissionException ex = new ReadHierarchyPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.DELETE))
      {
        DeleteHierarchyPermissionException ex = new DeleteHierarchyPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.CREATE))
      {
        CreateHierarchyPermissionException ex = new CreateHierarchyPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        throw ex;
      }
    }
  }
  
  protected boolean doesActorHavePermission(SingleActorDAOIF actor, Organization org, Operation op)
  {
    if (actor == null) // null actor is assumed to be SYSTEM
    {
      return true;
    }
    
    if (org != null)
    {
      String thisOrgCode = org.getCode();
      
      Set<RoleDAOIF> roles = actor.authorizedRoles();
      
      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();
        
        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String orgCode = RegistryRole.Type.parseOrgCode(roleName);
          
          if ( RegistryRole.Type.isRA_Role(roleName) )
          {
            return orgCode.equals(thisOrgCode) || op.equals(Operation.READ);
          }
          else if ( RegistryRole.Type.isRM_Role(roleName) && orgCode.equals(thisOrgCode) && op.equals(Operation.READ) )
          {
            return true;
          }
          else if ( (RegistryRole.Type.isAC_Role(roleName) || RegistryRole.Type.isRC_Role(roleName)) && orgCode.equals(thisOrgCode) && op.equals(Operation.READ))
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
  
  public boolean doesActorHaveRelationshipPermission(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode, boolean allowRC)
  {
    if (actor == null) // null actor is assumed to be SYSTEM
    {
      return true;
    }
    
    if (ht.getUniversalRelationship().getKey().equals(AllowedIn.CLASS)
        || ht.getUniversalRelationship().getKey().equals(LocatedIn.CLASS))
    {
      return true; // AllowedIn is deprecated and should not be used by the end-user.
    }
    
    Organization thisOrg = ht.getOrganization();
    
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
          else if ( RegistryRole.Type.isRM_Role(roleName) && orgCode.equals(thisOrgCode) )
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if ( parentGeoObjectTypeCode == null || childGeoObjectTypeCode == null // Null parent / child is used for GeoObjectEditor widget
                || gotCode.equals(parentGeoObjectTypeCode) || gotCode.equals(childGeoObjectTypeCode))
            {
              return true;
            }
          }
          else if ( allowRC && RegistryRole.Type.isRC_Role(roleName) && orgCode.equals(thisOrgCode) )
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if (gotCode.equals(parentGeoObjectTypeCode) || gotCode.equals(childGeoObjectTypeCode))
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
  
  /**
   * Throws an exception if the provided actor does not have permissions to this HierarchyType.
   * 
   * @param actor
   */
  public void enforceActorHasRelationshipPermission(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode, boolean allowRC)
  {
    if (!this.doesActorHaveRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, allowRC))
    {
      String parentLabel = ServerGeoObjectType.get(parentGeoObjectTypeCode).getLabel().getValue();
      String childLabel = ServerGeoObjectType.get(childGeoObjectTypeCode).getLabel().getValue();
      
      HierarchyRelationshipPermissionException ex = new HierarchyRelationshipPermissionException();
      ex.setParentGeoObjectType(parentLabel);
      ex.setChildGeoObjectType(childLabel);
      ex.setOrganization(ht.getOrganization().getDisplayLabel().getValue());
      throw ex;
    }
  }

  @Override
  public boolean canRead(SingleActorDAOIF actor, Organization org)
  {
    return this.doesActorHavePermission(actor, org, Operation.READ);
  }

  @Override
  public void enforceCanRead(SingleActorDAOIF actor, Organization org)
  {
    this.enforceActorHasPermission(actor, org, Operation.READ);
  }

  @Override
  public boolean canWrite(SingleActorDAOIF actor, Organization org)
  {
    return this.doesActorHavePermission(actor, org, Operation.WRITE);
  }

  @Override
  public void enforceCanWrite(SingleActorDAOIF actor, Organization org)
  {
    this.enforceActorHasPermission(actor, org, Operation.WRITE);
  }

  @Override
  public boolean canCreate(SingleActorDAOIF actor, Organization org)
  {
    return this.doesActorHavePermission(actor, org, Operation.CREATE);
  }

  @Override
  public void enforceCanCreate(SingleActorDAOIF actor, Organization org)
  {
    this.enforceActorHasPermission(actor, org, Operation.CREATE);
  }

  @Override
  public boolean canAddChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHaveRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, false);
  }

  @Override
  public void enforceCanAddChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, false);
  }

  @Override
  public boolean canAddChildCR(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHaveRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, true);
  }

  @Override
  public void enforceCanAddChildCR(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, true);
  }
  
  @Override
  public boolean canRemoveChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHaveRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, false);
  }

  @Override
  public void enforceCanRemoveChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, false);
  }

  @Override
  public boolean canRemoveChildCR(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHaveRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, true);
  }

  @Override
  public void enforceCanRemoveChildCR(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, true);
  }

  @Override
  public boolean canDelete(SingleActorDAOIF actor, Organization org)
  {
    return this.doesActorHavePermission(actor, org, Operation.DELETE);
  }

  @Override
  public void enforceCanDelete(SingleActorDAOIF actor, Organization org)
  {
    this.enforceActorHasPermission(actor, org, Operation.DELETE);
  }
  
}
