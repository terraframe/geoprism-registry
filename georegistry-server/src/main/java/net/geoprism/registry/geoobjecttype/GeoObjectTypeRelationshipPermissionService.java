package net.geoprism.registry.geoobjecttype;

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
import net.geoprism.registry.roles.GeoObjectAddChildPermissionException;
import net.geoprism.registry.roles.GeoObjectRemoveChildPermissionException;
import net.geoprism.registry.roles.GeoObjectViewRelationshipPermissionException;
import net.geoprism.registry.roles.HierarchyRelationshipPermissionException;

public class GeoObjectTypeRelationshipPermissionService implements GeoObjectTypeRelationshipPermissionServiceIF
{
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
  public boolean canRemoveChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.doesActorHaveRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, false);
  }

  @Override
  public void enforceCanRemoveChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.enforceActorHasRelationshipPermission(actor, ht, parentGeoObjectTypeCode, childGeoObjectTypeCode, false);
  }
}
