/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.metadata;

import java.io.Serializable;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RegistryRole implements Serializable
{

  public enum Type 
  {
    SRA(),
    
    RA(),

    RM(),
    
    RC(),
    
    AC();
   
    public static final String REGISTRY_ROLE_PREFIX             = "cgr";
    
    public static final String REGISTRY_ROOT_ORG_ROLE           = REGISTRY_ROLE_PREFIX + ".Org";

    /**
     * Constructs a role name for the {@link OrganizationDTO} with the given code.
     * 
     * @param organizationCode
     *          {@link OrganizationDTO} code.
     * 
     * @return role name for the {@link OrganizationDTO} with the given code.
     */
    public static String getRootOrgRoleName(String organizationCode)
    {
      return REGISTRY_ROOT_ORG_ROLE + "." + organizationCode;
    }
    
    /**
     * Returns true if the given role name is an {@link RegistryRole.Type#REGISTRY_ROOT_ORG_ROLE} role, false otherwise.
     * 
     * @param roleName
     * 
     * @return true if the given role name is an {@link RegistryRole.Type#REGISTRY_ROOT_ORG_ROLE} role, false otherwise.
     */
    public static boolean isRootOrgRole(String roleName)
    {
      if (roleName.equals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE))
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    
    /**
     * Returns true if the given role name is related to a {@link OrganizationDTO}, false otherwise.
     * 
     * @param roleName
     * 
     * @return true if the given role name is related to a {@link OrganizationDTO}, false otherwise.
     */
    public static boolean isOrgRole(String roleName)
    {      
      if (roleName.contains(Type.REGISTRY_ROOT_ORG_ROLE))
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    
    /**
     * Returns the code for the {@link GeoObjectType} that is in the role name.
     * 
     * Precondition: the given role name represents a role that has a GeoObjectType code in it.
     * 
     * @param roleName
     * 
     * @return the code for the {@link GeoObjectType} that is in the role name.
     */
    public static String parseGotCode(String roleName)
    {
      String[] strArray = roleName.split("\\.");
      
      String gotCode = strArray[3];
      
      return gotCode;
    }
    
    /**
     * Returns the code for the {@link OrganizationDTO} that is in the role name.
     * 
     * Precondition: the given role name represents a role that has an organization code in it.
     * 
     * @param roleName
     * 
     * @return the code for the {@link OrganizationDTO} that is in the role name.
     */
    public static String parseOrgCode(String roleName)
    {
      String[] strArray = roleName.split("\\.");
      
      String organizationCode = strArray[2];
      
      return organizationCode;
    }
    
    /**
     * Returns the {@link RegistryRole} name for the Super Registry Administrator.
     * 
     * 
     * @return the {@link RegistryRole} name for the Super Registry Administrator.
     */
    public static String getSRA_RoleName()
    {
      return REGISTRY_ROLE_PREFIX + "." + Type.SRA.name();
    }
    
    /**
     * Returns true if the given role name is an {@link RegistryRole.Type#SRA} role, false otherwise.
     * 
     * @param roleName
     * 
     * @return true if the given role name is an {@link RegistryRole.Type#SRA} role, false otherwise.
     */
    public static boolean isSRA_Role(String roleName)
    {
      boolean isValidRole = false;
      
      String[] strArray = roleName.split("\\.");
      
      try
      {
        String cgrRoleNamespace = strArray[0];
        
        if (strArray.length == 2 && cgrRoleNamespace.equals(RegistryRole.Type.REGISTRY_ROLE_PREFIX))
        {
          String roleSuffix = strArray[1];
          
          if (roleSuffix.equals(RegistryRole.Type.SRA.name()))
          {
            isValidRole = true;
          }
        }
      }
      catch (StringIndexOutOfBoundsException e) {}

      return isValidRole;
    }
    
    /**
     * Constructs a {@link RegistryRole} for the Registry Administrator for the
     * {@link OrganizationDTO} with the given code.
     * 
     * @param organizationCode
     *          {@link OrganizationDTO} code.
     * 
     * @return {@link RegistryRole} name for the Registry Administrator for the
     *         {@link OrganizationDTO} with the given code.
     */
    public static String getRA_RoleName(String organizationCode)
    {
      String organizationRoleName = RegistryRole.Type.getRootOrgRoleName(organizationCode);

      return organizationRoleName + "." + Type.RA.name();
    }
    
    /**
     * Returns true if the given role name is an {@link RegistryRole.Type#RA} role, false otherwise.
     * 
     * Precondition: assumes the organization code in the role name is valid.
     * 
     * @param roleName
     * 
     * @return true if the given role name is an {@link RegistryRole.Type#RA} role, false otherwise.
     */
    public static boolean isRA_Role(String roleName)
    {
      boolean isValidRole = false;
      
      String[] strArray = roleName.split("\\.");
      
      try
      {      
        if (strArray.length == 4)
        {        
          String cgrRoleNamespace = strArray[0];
          cgrRoleNamespace += "."+strArray[1];
          
          if (cgrRoleNamespace.equals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE))
          {
            // Assume the org code is valid
            String roleSuffix = strArray[3];
          
            if (roleSuffix.equals(RegistryRole.Type.RA.name()))
            {
              isValidRole = true;
            }
          }
        }
      }
      catch (StringIndexOutOfBoundsException e) {}
      
      return isValidRole;
    }
    
    /**
     * Constructs a {@link RegistryRole} for the Registry Maintainer for the
     * {@link OrganizationDTO} with the given code and {@link GeoObjectType} with the given code.
     * 
     * @param organizationCode
     *          {@link OrganizationDTO} code.
     *          
     * @param geoObjectTypeCode
     *          {@link GeoObjectType} code.
     * 
     * @return {@link RegistryRole} name for the Registry Maintainer for the
     * {@link OrganizationDTO} with the given code and {@link GeoObjectType} with the given code.
     */
    public static String getRM_RoleName(String organizationCode, String geoObjectTypeCode)
    {
      String organizationRoleName = RegistryRole.Type.getRootOrgRoleName(organizationCode);

      return organizationRoleName + "." + geoObjectTypeCode+"."+Type.RM.name();
    }
    
    
    /**
     * Returns true if the given role name is an {@link RegistryRole.Type#RM}  role, false otherwise.
     * 
     * Precondition: assumes the {@link OrganizationDTO} code in the role name is valid.
     * Precondition: assumes the {@link GeoObjectType} code in the role name is valid.
     * 
     * @param roleName
     * 
     * @return true if the given role name is an {@link RegistryRole.Type#RM} role, false otherwise.
     */
    public static boolean isRM_Role(String roleName)
    {
      boolean isValidRole = false;
      
      String[] strArray = roleName.split("\\.");
      
      try
      {      
        if (strArray.length == 5)
        {        
          String cgrRoleNamespace = strArray[0];
          cgrRoleNamespace += "."+strArray[1];
          
          if (cgrRoleNamespace.equals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE))
          {
            // Assume the org code is valid
            // Assume the geo object type code is valid
            String roleSuffix = strArray[4];
          
            if (roleSuffix.equals(RegistryRole.Type.RM.name()))
            {
              isValidRole = true;
            }
          }
        }
      }
      catch (StringIndexOutOfBoundsException e) {}
      
      return isValidRole;
    }
    
    /**
     * Constructs a {@link RegistryRole} for the Registry Contributor for the
     * {@link OrganizationDTO} with the given code and {@link GeoObjectType} with the given code.
     * 
     * @param organizationCode
     *          {@link OrganizationDTO} code.
     *          
     * @param geoObjectTypeCode
     *          {@link GeoObjectType} code.
     * 
     * @return {@link RegistryRole} name for the Registry Contributor for the
     * {@link OrganizationDTO} with the given code and {@link GeoObjectType} with the given code.
     */
    public static String getRC_RoleName(String organizationCode, String geoObjectTypeCode)
    {
      String organizationRoleName = RegistryRole.Type.getRootOrgRoleName(organizationCode);

      return organizationRoleName + "." + geoObjectTypeCode+"."+Type.RC.name();
    }
    
    
    /**
     * Returns true if the given role name is an {@link RegistryRole.Type#RC} role, false otherwise.
     * 
     * Precondition: assumes the {@link OrganizationDTO} code in the role name is valid.
     * Precondition: assumes the {@link GeoObjectType} code in the role name is valid.
     * 
     * @param roleName
     * 
     * @return true if the given role name is an {@link RegistryRole.Type#RC} role, false otherwise.
     */
    public static boolean isRC_Role(String roleName)
    {
      boolean isValidRole = false;
      
      String[] strArray = roleName.split("\\.");
      
      try
      {      
        if (strArray.length == 5)
        {        
          String cgrRoleNamespace = strArray[0];
          cgrRoleNamespace += "."+strArray[1];
          
          if (cgrRoleNamespace.equals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE))
          {
            // Assume the org code is valid
            // Assume the geo object type code is valid
            String roleSuffix = strArray[4];
          
            if (roleSuffix.equals(RegistryRole.Type.RC.name()))
            {
              isValidRole = true;
            }
          }
        }
      }
      catch (StringIndexOutOfBoundsException e) {}
      
      return isValidRole;
    }

    /**
     * Returns true if the given role name is an {@link RegistryRole.Type#AC} role, false otherwise.
     * 
     * Precondition: assumes the {@link OrganizationDTO} code in the role name is valid.
     * Precondition: assumes the {@link GeoObjectType} code in the role name is valid.
     * 
     * @param roleName
     * 
     * @return true if the given role name is an {@link RegistryRole.Type#AC} role, false otherwise.
     */
    public static boolean isAC_Role(String roleName)
    {
      boolean isValidRole = false;
      
      String[] strArray = roleName.split("\\.");
      
      try
      {      
        if (strArray.length == 5)
        {        
          String cgrRoleNamespace = strArray[0];
          cgrRoleNamespace += "."+strArray[1];
          
          if (cgrRoleNamespace.equals(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE))
          {
            // Assume the org code is valid
            // Assume the geo object type code is valid
            String roleSuffix = strArray[4];
          
            if (roleSuffix.equals(RegistryRole.Type.AC.name()))
            {
              isValidRole = true;
            }
          }
        }
      }
      catch (StringIndexOutOfBoundsException e) {}
      
      return isValidRole;
    }
    
    /**
     * Constructs a {@link RegistryRole} for the API Contributor for the
     * {@link OrganizationDTO} with the given code and {@link GeoObjectType} with the given code.
     * 
     * @param organizationCode
     *          {@link OrganizationDTO} code.
     *          
     * @param geoObjectTypeCode
     *          {@link GeoObjectType} code.
     * 
     * @return {@link RegistryRole} name for the API Contributor for the
     * {@link OrganizationDTO} with the given code and {@link GeoObjectType} with the given code.
     */
    public static String getAC_RoleName(String organizationCode, String geoObjectTypeCode)
    {
      String organizationRoleName = RegistryRole.Type.getRootOrgRoleName(organizationCode);

      return organizationRoleName + "." + geoObjectTypeCode+"."+Type.AC.name();
    }
  }

  
  /**
   * 
   */
  private static final long serialVersionUID = -1535218580445132712L;

  public static final String         JSON_TYPE                   = "type";
  
  public static final String         JSON_NAME                   = "name";
  
  public static final String         JSON_LOCALIZED_LABEL        = "label";
  
  public static final String         JSON_ORG_CODE               = "orgCode";
  
  public static final String         JSON_ORG_LABEL              = "orgLabel";
  
  public static final String         JSON_GEO_OBJECT_TYPE_CODE   = "geoObjectTypeCode";
  
  public static final String         JSON_GEO_OBJECT_TYPE_LABEL  = "geoObjectTypeLabel";
  
  public static final String         JSON_ASSIGNED               = "assigned";
 
  private Type                       type;
  
  private String                     name;
  
  private LocalizedValue             label;
  
  private String                     organizationCode;
  
  private LocalizedValue             organizationLabel;
  
  private String                     geoObjectTypeCode;
  
  private LocalizedValue             geoObjectTypeLabel;
  
  private boolean                    assigned;
  
  /**
   * Precondition: Parameters form a valid role with the following rules:
   * 
   * {@link RegistryRole.Type.RA}
   * 
   * @param type {@link Type} of the {@link RegistryRole}
   * @param name  name of the role
   * @param label localized display label of the role
   * @param organizationCode the organization the role belongs to (if any)
   * @param geoObjectTypeCode the {@link GeoObjectType} that the role is associated with (if any);
   */
  private RegistryRole(Type type, String name, LocalizedValue label, String organizationCode, String geoObjectTypeCode, boolean assigned)
  {
    this.type               = type;
    this.label              = label;
    this.assigned           = assigned;
    this.organizationLabel  = LocalizedValue.createEmptyLocalizedValue();
    this.geoObjectTypeLabel = LocalizedValue.createEmptyLocalizedValue();

    if (this.type.equals(Type.SRA))
    {
      this.organizationCode  = "";
      this.geoObjectTypeCode = "";
      this.name = Type.getSRA_RoleName();
    }
    else if (this.type.equals(Type.RA))
    {
      this.organizationCode  = organizationCode;
      this.geoObjectTypeCode = "";
      this.name              = Type.getRA_RoleName(this.organizationCode);
    }
    else if (this.type.equals(Type.RM))
    {
      this.organizationCode  = organizationCode;
      this.geoObjectTypeCode = geoObjectTypeCode;
      this.name              = Type.getRM_RoleName(this.organizationCode, this.geoObjectTypeCode);
    }
    else if (this.type.equals(Type.RC))
    {
      this.organizationCode  = organizationCode;
      this.geoObjectTypeCode = geoObjectTypeCode;
      this.name              = Type.getRC_RoleName(this.organizationCode, this.geoObjectTypeCode);
    }
    else if (this.type.equals(Type.AC))
    {
      this.organizationCode  = organizationCode;
      this.geoObjectTypeCode = geoObjectTypeCode;
      this.name              = Type.getAC_RoleName(this.organizationCode, this.geoObjectTypeCode);
    }
    else
    {
      this.name              = name;
      this.label             = label;
      this.organizationCode  = organizationCode;
      this.geoObjectTypeCode = geoObjectTypeCode;
    }
  }
  
  
  /**
   * Creates a {@link RegistryRole} object for the SRA role.
   * 
   * @param label localized display label.
   * 
   * @return a {@link RegistryRole} object for the SRA role.
   */
  public static RegistryRole createSRA(LocalizedValue label)
  {
    String roleName = RegistryRole.Type.getSRA_RoleName();
    
    return new RegistryRole(RegistryRole.Type.SRA, roleName, label, "", "", false);
  }
  
  /**
   * Creates a {@link RegistryRole} object for the RA role for the {@link OrganizationDTO} with the given code.
   * 
   * @param label
   * @param organizationCode
   * @return {@link RegistryRole} object for the RA role for the {@link OrganizationDTO} with the given code.
   */
  public static RegistryRole createRA(LocalizedValue label, String organizationCode)
  {
    String roleName = RegistryRole.Type.getRA_RoleName(organizationCode);
    
    return new RegistryRole(RegistryRole.Type.RA, roleName, label, organizationCode, "", false);
  }
  
  /**
   * Creates a {@link RegistryRole} object for the RM role for the {@link OrganizationDTO} with the given code
   * and {@link GeoObjectType} with the given code.
   * 
   * @param label
   * @param organizationCode
   * @return {@link RegistryRole} object for the RM role for the {@link OrganizationDTO} with the given code
   * and {@link GeoObjectType} with the given code.
   */
  public static RegistryRole createRM(LocalizedValue label, String organizationCode, String geoObjectTypeCode)
  {
    String roleName = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectTypeCode);
    
    return new RegistryRole(RegistryRole.Type.RM, roleName, label, organizationCode, geoObjectTypeCode, false);
  }
  
  /**
   * Creates a {@link RegistryRole} object for the RC role for the {@link OrganizationDTO} with the given code
   * and {@link GeoObjectType} with the given code.
   * 
   * @param label
   * @param organizationCode
   * @return {@link RegistryRole} object for the RC role for the {@link OrganizationDTO} with the given code
   * and {@link GeoObjectType} with the given code.
   */
  public static RegistryRole createRC(LocalizedValue label, String organizationCode, String geoObjectTypeCode)
  {
    String roleName = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectTypeCode);
    
    return new RegistryRole(RegistryRole.Type.RC, roleName, label, organizationCode, geoObjectTypeCode, false);
  }
  
  /**
   * Creates a {@link RegistryRole} object for the AC role for the {@link OrganizationDTO} with the given code
   * and {@link GeoObjectType} with the given code.
   * 
   * @param label
   * @param organizationCode
   * @return {@link RegistryRole} object for the AC role for the {@link OrganizationDTO} with the given code
   * and {@link GeoObjectType} with the given code.
   */
  public static RegistryRole createAC(LocalizedValue label, String organizationCode, String geoObjectTypeCode)
  {
    String roleName = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectTypeCode);
    
    return new RegistryRole(RegistryRole.Type.AC, roleName, label, organizationCode, geoObjectTypeCode, false);
  }
  
  /**
   * Returns the {@link Type} of the role.
   * 
   * @return the {@link Type} of the role.
   */
  public Type getType()
  {
    return this.type;
  }
  
  /**
   * Returns the name of this {@link RegistryRole}.
   * 
   * @return name of this {@link RegistryRole}.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Returns the localized label of this {@link RegistryRole} used for the
   * presentation layer.
   * 
   * @return Localized label of this {@link RegistryRole}.
   */
  public LocalizedValue getLabel()
  {
    return this.label;
  }
  
  /**
   * Sets the localized display label of this {@link RegistryRole}.
   * 
   * Precondition: label may not be null
   * 
   * @param label
   */
  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }
  
  
  /**
   * Sets the localized display label of this {@link RegistryRole}.
   * 
   * Precondition: key may not be null
   * Precondition: key must represent a valid locale that has been defined on the back-end
   * 
   * @param key string of the locale name.
   * @param value value for the given locale.
   */
  public void setLabel(String key, String value)
  {
    this.label.setValue(key, value);
  }
  
  /**
   * Returns the {@link OrganizationDTO} code of this {@link RegistryRole} or an empty string if there is none.
   * 
   * @return the {@link OrganizationDTO} code of this {@link RegistryRole} or an empty string if there is none.
   */
  public String getOrganizationCode()
  {
    return this.organizationCode;
  }
  
  /**
   * Returns the localized label of the {@link OrganizationDTO}.
   * 
   * @return the localized label of the {@link OrganizationDTO}.
   */
  public LocalizedValue getOrganizationLabel()
  {
    return this.organizationLabel;
  }
  
  /**
   * Sets the localized display label of this {@link OrganizationDTO}.
   * 
   * Precondition: label may not be null
   * 
   * @param label
   */
  public void setOrganizationLabel(LocalizedValue label)
  {
    this.organizationLabel = label;
  }
  
  
  /**
   * Sets the localized display label of the {@link OrganizationDTO}.
   * 
   * Precondition: key may not be null
   * Precondition: key must represent a valid locale that has been defined on the back-end
   * 
   * @param key string of the locale name.
   * @param value value for the given locale.
   */
  public void setOrganizationLabel(String key, String value)
  {
    this.organizationLabel.setValue(key, value);
  }
  
  /**
   * Returns the {@link GeoObjectType} code of this {@link RegistryRole} or an empty string if there is none.
   * 
   * @return the {@link GeoObjectType} code of this {@link RegistryRole} or an empty string if there is none.
   */
  public String getGeoObjectTypeCode()
  {
    return this.geoObjectTypeCode;
  }
  
  
  /**
   * Returns the localized label of the {@link GeoObjectType}.
   * 
   * @return the localized label of the {@link GeoObjectType}.
   */
  public LocalizedValue getGeoObjectTypeLabel()
  {
    return this.geoObjectTypeLabel;
  }
  
  /**
   * Sets the localized display label of this {@link GeoObjectType}.
   * 
   * Precondition: label may not be null
   * 
   * @param label
   */
  public void setGeoObjectTypeLabel(LocalizedValue label)
  {
    this.geoObjectTypeLabel = label;
  }
  
  
  /**
   * Sets the localized display label of the {@link GeoObjectType}.
   * 
   * Precondition: key may not be null
   * Precondition: key must represent a valid locale that has been defined on the back-end
   * 
   * @param key string of the locale name.
   * @param value value for the given locale.
   */
  public void setGeoObjectTypeLabel(String key, String value)
  {
    this.geoObjectTypeLabel.setValue(key, value);
  }
  
  /**
   * This is used in the context to represent whether a user is assigned to this role.
   * 
   * @return true if it is assigned in the context in which it is called, false otherwise.
   */
  public boolean isAssigned()
  {
    return this.assigned;
  }
  
  /**
   * Sets whether the role is assigned in the context this object is being used.
   * 
   * @param assigned
   */
  public void setAssigned(boolean assigned)
  {
    this.assigned = assigned;
  }
  
  /**
   * Creates a {@link RegistryRole} from the given JSON string.
   * 
   * Precondition: 
   * 
   * @param sJson
   *          JSON string that defines the {@link RegistryRole}.
   * @return
   */
  public static RegistryRole fromJSON(String sJson)
  {
    JsonParser parser = new JsonParser();

    JsonObject oJson = parser.parse(sJson).getAsJsonObject();
    
    String type = oJson.get(JSON_TYPE).getAsString().toUpperCase();
    
    String name = oJson.get(JSON_NAME).getAsString();

    LocalizedValue label = LocalizedValue.fromJSON(oJson.get(JSON_LOCALIZED_LABEL).getAsJsonObject());
    
    String organizationCode = oJson.get(JSON_ORG_CODE).getAsString();
    
    String geoObjectTypeCode = oJson.get(JSON_GEO_OBJECT_TYPE_CODE).getAsString();
    
    JsonElement assignedElement = oJson.get(JSON_ASSIGNED);
    
    boolean assigned = false;

    if (assignedElement != null)
    {
      assigned = assignedElement.getAsBoolean();
    }
    
    RegistryRole registryRole = new RegistryRole(Type.valueOf(type), name, label, organizationCode, geoObjectTypeCode, assigned);
    
    JsonElement orgLabelElement = oJson.get(JSON_ORG_LABEL);
    if (orgLabelElement != null)
    {
      LocalizedValue orgLabel = LocalizedValue.fromJSON(orgLabelElement.getAsJsonObject());
      registryRole.setOrganizationLabel(orgLabel);
    }
    
    JsonElement geoObjTypeLabelElement = oJson.get(JSON_GEO_OBJECT_TYPE_LABEL);
    if (geoObjTypeLabelElement != null)
    {
      LocalizedValue geoOrgTypeLabel = LocalizedValue.fromJSON(geoObjTypeLabelElement.getAsJsonObject());
      registryRole.setGeoObjectTypeLabel(geoOrgTypeLabel);
    }
    
    return registryRole;
  }
  
  
  /**
   * Return the JSON representation of this {@link RegistryRole}.
   * 
   * @return JSON representation of this {@link RegistryRole}.
   */
  public final JsonObject toJSON()
  {
    return toJSON(new DefaultSerializer());
  }
  
  /**
   * Return the JSON representation of this {@link RegistryRole}. Filters the
   * attributes to include in serialization.
   * 
   * @param filter
   *          Filter used to determine if an attribute is included
   * 
   * @return JSON representation of this {@link RegistryRole}.
   */
  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject json = new JsonObject();
    
    json.addProperty(JSON_TYPE, this.getType().name());
    
    json.addProperty(JSON_NAME, this.getName());
    
    json.add(JSON_LOCALIZED_LABEL, this.getLabel().toJSON(serializer));
    
    json.addProperty(JSON_ORG_CODE, this.getOrganizationCode());
    
    json.add(JSON_ORG_LABEL, this.getOrganizationLabel().toJSON(serializer));
    
    json.addProperty(JSON_GEO_OBJECT_TYPE_CODE, this.getGeoObjectTypeCode());
    
    json.add(JSON_GEO_OBJECT_TYPE_LABEL, this.getGeoObjectTypeLabel().toJSON(serializer));
    
    json.add(JSON_GEO_OBJECT_TYPE_LABEL, this.getGeoObjectTypeLabel().toJSON(serializer));
    
    json.addProperty(JSON_ASSIGNED, this.isAssigned());

    return json;
  }
}
