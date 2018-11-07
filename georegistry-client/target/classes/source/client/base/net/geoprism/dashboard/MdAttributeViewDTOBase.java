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
package net.geoprism.dashboard;

@com.runwaysdk.business.ClassSignature(hash = 909903717)
public abstract class MdAttributeViewDTOBase extends com.runwaysdk.business.ViewDTO 
{
  public final static String CLASS = "net.geoprism.dashboard.MdAttributeView";
  private static final long serialVersionUID = 909903717;
  
  protected MdAttributeViewDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ATTRIBUTENAME = "attributeName";
  public static java.lang.String ATTRIBUTETYPE = "attributeType";
  public static java.lang.String DISPLAYLABEL = "displayLabel";
  public static java.lang.String OID = "oid";
  public static java.lang.String MDATTRIBUTEID = "mdAttributeId";
  public static java.lang.String MDCLASSID = "mdClassId";
  public String getAttributeName()
  {
    return getValue(ATTRIBUTENAME);
  }
  
  public void setAttributeName(String value)
  {
    if(value == null)
    {
      setValue(ATTRIBUTENAME, "");
    }
    else
    {
      setValue(ATTRIBUTENAME, value);
    }
  }
  
  public boolean isAttributeNameWritable()
  {
    return isWritable(ATTRIBUTENAME);
  }
  
  public boolean isAttributeNameReadable()
  {
    return isReadable(ATTRIBUTENAME);
  }
  
  public boolean isAttributeNameModified()
  {
    return isModified(ATTRIBUTENAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getAttributeNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(ATTRIBUTENAME).getAttributeMdDTO();
  }
  
  public String getAttributeType()
  {
    return getValue(ATTRIBUTETYPE);
  }
  
  public void setAttributeType(String value)
  {
    if(value == null)
    {
      setValue(ATTRIBUTETYPE, "");
    }
    else
    {
      setValue(ATTRIBUTETYPE, value);
    }
  }
  
  public boolean isAttributeTypeWritable()
  {
    return isWritable(ATTRIBUTETYPE);
  }
  
  public boolean isAttributeTypeReadable()
  {
    return isReadable(ATTRIBUTETYPE);
  }
  
  public boolean isAttributeTypeModified()
  {
    return isModified(ATTRIBUTETYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getAttributeTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(ATTRIBUTETYPE).getAttributeMdDTO();
  }
  
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
  
  public String getMdAttributeId()
  {
    return getValue(MDATTRIBUTEID);
  }
  
  public void setMdAttributeId(String value)
  {
    if(value == null)
    {
      setValue(MDATTRIBUTEID, "");
    }
    else
    {
      setValue(MDATTRIBUTEID, value);
    }
  }
  
  public boolean isMdAttributeIdWritable()
  {
    return isWritable(MDATTRIBUTEID);
  }
  
  public boolean isMdAttributeIdReadable()
  {
    return isReadable(MDATTRIBUTEID);
  }
  
  public boolean isMdAttributeIdModified()
  {
    return isModified(MDATTRIBUTEID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getMdAttributeIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(MDATTRIBUTEID).getAttributeMdDTO();
  }
  
  public String getMdClassId()
  {
    return getValue(MDCLASSID);
  }
  
  public void setMdClassId(String value)
  {
    if(value == null)
    {
      setValue(MDCLASSID, "");
    }
    else
    {
      setValue(MDCLASSID, value);
    }
  }
  
  public boolean isMdClassIdWritable()
  {
    return isWritable(MDCLASSID);
  }
  
  public boolean isMdClassIdReadable()
  {
    return isReadable(MDCLASSID);
  }
  
  public boolean isMdClassIdModified()
  {
    return isModified(MDCLASSID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getMdClassIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(MDCLASSID).getAttributeMdDTO();
  }
  
  public static MdAttributeViewDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.ViewDTO dto = (com.runwaysdk.business.ViewDTO)clientRequest.get(oid);
    
    return (MdAttributeViewDTO) dto;
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
