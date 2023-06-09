/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.conversion;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.system.Roles;

public class RegistryRoleConverter
{

  public RegistryRoleConverter()
  {
  }
  
  /**
   * Returns true if the given role is a registry-related role, false otherwise.
   * 
   * @param role RunwaySDK role
   * 
   * @return true if the given role is a registry-related role, false otherwise.
   */
  public boolean isRegsitryRole(Roles role)
  {
    String roleName = role.getRoleName();
    
    if (roleName.indexOf(RegistryRole.Type.REGISTRY_ROLE_PREFIX) == 0)
    {
      return true;
    }
    else
    {
      return false;
    }
    
  }
  
  /**
   * Returns the {@link RegistryRole} object converted from the {@link Roles} object, or NULL if it is not a registry role. 
   * 
   * @param role RunwaySDK role.
   * 
   * @return the {@link RegistryRole} object converted from the {@link Roles} object, or NULL if it is not a registry role. 
   */
  public RegistryRole build(Roles role)
  {
    String roleName = role.getRoleName();
    
    if (RegistryRole.Type.isSRA_Role(roleName))
    {
      return this.buildSRA_Role(role);
    }
    else if (RegistryRole.Type.isRA_Role(roleName))
    {
      return this.buildRA_Role(role);
    }
    else if (RegistryRole.Type.isRM_Role(roleName))
    {
      return this.buildRM_Role(role);
    }
    else if (RegistryRole.Type.isRC_Role(roleName))
    {
      return this.buildRC_Role(role);
    }
    else if (RegistryRole.Type.isAC_Role(roleName))
    {
      return this.buildAC_Role(role);
    }
    
    return null;
  }
  
  /**
   * Returns the {@link RegistryRole} object for the given SRA {@link Roles}.
   * 
   * Precondition: assumes the roleName is a valid SRA role
   * 
   * @param role RunwaySDK role
   * 
   * @return {@link RegistryRole}
   */
  private RegistryRole buildSRA_Role(Roles sraRole)
  {
    LocalizedValue localizedValue = RegistryLocalizedValueConverter.convert(sraRole.getDisplayLabel());

    return RegistryRole.createSRA(localizedValue);
  }
  
  /**
   * Returns the {@link RegistryRole} object for the given RA {@link Roles}.
   * 
   * Precondition: assumes the roleName is a valid RA role
   * 
   * @param role RunwaySDK role
   * 
   * @return {@link RegistryRole}
   */
  private RegistryRole buildRA_Role(Roles raRole)
  {
    LocalizedValue localizedValue = RegistryLocalizedValueConverter.convert(raRole.getDisplayLabel());
    
    String[] strArray = raRole.getRoleName().split("\\.");
    
    String organizationCode = strArray[2];

    return RegistryRole.createRA(localizedValue, organizationCode);
  }
  
  /**
   * Returns the {@link RegistryRole} object for the given RM {@link Roles}.
   * 
   * Precondition: assumes the roleName is a valid RM role
   * 
   * @param role RunwaySDK role
   * 
   * @return {@link RegistryRole}
   */
  private RegistryRole buildRM_Role(Roles raRole)
  {
    LocalizedValue localizedValue = RegistryLocalizedValueConverter.convert(raRole.getDisplayLabel());
    
    String[] strArray = raRole.getRoleName().split("\\.");
    
    String organizationCode = strArray[2];
    
    String geoObjectTypeCode = strArray[3];

    return RegistryRole.createRM(localizedValue, organizationCode, geoObjectTypeCode);
  }
  
  
  /**
   * Returns the {@link RegistryRole} object for the given RC {@link Roles}.
   * 
   * Precondition: assumes the roleName is a valid RC role
   * 
   * @param role RunwaySDK role
   * 
   * @return {@link RegistryRole}
   */
  private RegistryRole buildRC_Role(Roles raRole)
  {
    LocalizedValue localizedValue = RegistryLocalizedValueConverter.convert(raRole.getDisplayLabel());
    
    String[] strArray = raRole.getRoleName().split("\\.");
    
    String organizationCode = strArray[2];
    
    String geoObjectTypeCode = strArray[3];

    return RegistryRole.createRC(localizedValue, organizationCode, geoObjectTypeCode);
  }
  
  /**
   * Returns the {@link RegistryRole} object for the given RC {@link Roles}.
   * 
   * Precondition: assumes the roleName is a valid RC role
   * 
   * @param role RunwaySDK role
   * 
   * @return {@link RegistryRole}
   */
  private RegistryRole buildAC_Role(Roles raRole)
  {
    LocalizedValue localizedValue = RegistryLocalizedValueConverter.convert(raRole.getDisplayLabel());
    
    String[] strArray = raRole.getRoleName().split("\\.");
    
    String organizationCode = strArray[2];
    
    String geoObjectTypeCode = strArray[3];

    return RegistryRole.createAC(localizedValue, organizationCode, geoObjectTypeCode);
  }
}
