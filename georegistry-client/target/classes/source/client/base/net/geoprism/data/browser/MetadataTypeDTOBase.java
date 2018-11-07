/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.data.browser;

@com.runwaysdk.business.ClassSignature(hash = -1447059302)
public abstract class MetadataTypeDTOBase extends com.runwaysdk.business.ViewDTO 
{
  public final static String CLASS = "net.geoprism.data.browser.MetadataType";
  private static final long serialVersionUID = -1447059302;
  
  protected MetadataTypeDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String DISPLAYLABEL = "displayLabel";
  public static java.lang.String OID = "oid";
  public static java.lang.String PARENTTYPEID = "parentTypeId";
  public static java.lang.String TYPEID = "typeId";
  public static java.lang.String TYPENAME = "typeName";
  public static java.lang.String TYPEPACKAGE = "typePackage";
  public String getDisplayLabel()
  {
    return getValue(DISPLAYLABEL);
  }
  
  public void setDisplayLabel(String value)
  {
    if(value == null)
    {
      setValue(DISPLAYLABEL, "");
    }
    else
    {
      setValue(DISPLAYLABEL, value);
    }
  }
  
  public boolean isDisplayLabelWritable()
  {
    return isWritable(DISPLAYLABEL);
  }
  
  public boolean isDisplayLabelReadable()
  {
    return isReadable(DISPLAYLABEL);
  }
  
  public boolean isDisplayLabelModified()
  {
    return isModified(DISPLAYLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getDisplayLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(DISPLAYLABEL).getAttributeMdDTO();
  }
  
  public String getParentTypeId()
  {
    return getValue(PARENTTYPEID);
  }
  
  public void setParentTypeId(String value)
  {
    if(value == null)
    {
      setValue(PARENTTYPEID, "");
    }
    else
    {
      setValue(PARENTTYPEID, value);
    }
  }
  
  public boolean isParentTypeIdWritable()
  {
    return isWritable(PARENTTYPEID);
  }
  
  public boolean isParentTypeIdReadable()
  {
    return isReadable(PARENTTYPEID);
  }
  
  public boolean isParentTypeIdModified()
  {
    return isModified(PARENTTYPEID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getParentTypeIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(PARENTTYPEID).getAttributeMdDTO();
  }
  
  public String getTypeId()
  {
    return getValue(TYPEID);
  }
  
  public void setTypeId(String value)
  {
    if(value == null)
    {
      setValue(TYPEID, "");
    }
    else
    {
      setValue(TYPEID, value);
    }
  }
  
  public boolean isTypeIdWritable()
  {
    return isWritable(TYPEID);
  }
  
  public boolean isTypeIdReadable()
  {
    return isReadable(TYPEID);
  }
  
  public boolean isTypeIdModified()
  {
    return isModified(TYPEID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getTypeIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(TYPEID).getAttributeMdDTO();
  }
  
  public String getTypeName()
  {
    return getValue(TYPENAME);
  }
  
  public void setTypeName(String value)
  {
    if(value == null)
    {
      setValue(TYPENAME, "");
    }
    else
    {
      setValue(TYPENAME, value);
    }
  }
  
  public boolean isTypeNameWritable()
  {
    return isWritable(TYPENAME);
  }
  
  public boolean isTypeNameReadable()
  {
    return isReadable(TYPENAME);
  }
  
  public boolean isTypeNameModified()
  {
    return isModified(TYPENAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getTypeNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(TYPENAME).getAttributeMdDTO();
  }
  
  public String getTypePackage()
  {
    return getValue(TYPEPACKAGE);
  }
  
  public void setTypePackage(String value)
  {
    if(value == null)
    {
      setValue(TYPEPACKAGE, "");
    }
    else
    {
      setValue(TYPEPACKAGE, value);
    }
  }
  
  public boolean isTypePackageWritable()
  {
    return isWritable(TYPEPACKAGE);
  }
  
  public boolean isTypePackageReadable()
  {
    return isReadable(TYPEPACKAGE);
  }
  
  public boolean isTypePackageModified()
  {
    return isModified(TYPEPACKAGE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getTypePackageMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(TYPEPACKAGE).getAttributeMdDTO();
  }
  
  public static MetadataTypeDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.ViewDTO dto = (com.runwaysdk.business.ViewDTO)clientRequest.get(oid);
    
    return (MetadataTypeDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createSessionComponent(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
}
