/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
        // SRA only has the ability to see types and hierarchies, it does not
        // have permissions to modify
        else if (RegistryRole.Type.isSRA_Role(roleName))
        {
//          return true;
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
