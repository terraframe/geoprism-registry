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

@com.runwaysdk.business.ClassSignature(hash = -413858993)
public abstract class DashboardLegendDTOBase extends com.runwaysdk.business.StructDTO 
{
  public final static String CLASS = "net.geoprism.dashboard.DashboardLegend";
  private static final long serialVersionUID = -413858993;
  
  protected DashboardLegendDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given StructDTO into a new DTO.
  * 
  * @param structDTO The StructDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected DashboardLegendDTOBase(com.runwaysdk.business.StructDTO structDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(structDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String GROUPEDINLEGEND = "groupedInLegend";
  public static java.lang.String OID = "oid";
  public static java.lang.String IMAGESNAPSHOT = "imageSnapshot";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LEGENDXPOSITION = "legendXPosition";
  public static java.lang.String LEGENDYPOSITION = "legendYPosition";
  public static java.lang.String SITEMASTER = "siteMaster";
  public Boolean getGroupedInLegend()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(GROUPEDINLEGEND));
  }
  
  public void setGroupedInLegend(Boolean value)
  {
    if(value == null)
    {
      setValue(GROUPEDINLEGEND, "");
    }
    else
    {
      setValue(GROUPEDINLEGEND, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isGroupedInLegendWritable()
  {
    return isWritable(GROUPEDINLEGEND);
  }
  
  public boolean isGroupedInLegendReadable()
  {
    return isReadable(GROUPEDINLEGEND);
  }
  
  public boolean isGroupedInLegendModified()
  {
    return isModified(GROUPEDINLEGEND);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getGroupedInLegendMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(GROUPEDINLEGEND).getAttributeMdDTO();
  }
  
  public byte[] getImageSnapshot()
  {
    return super.getBlob(IMAGESNAPSHOT);
  }
  
  public void setImageSnapshot(byte[] bytes)
  {
    super.setBlob(IMAGESNAPSHOT, bytes);
  }
  
  public boolean isImageSnapshotWritable()
  {
    return isWritable(IMAGESNAPSHOT);
  }
  
  public boolean isImageSnapshotReadable()
  {
    return isReadable(IMAGESNAPSHOT);
  }
  
  public boolean isImageSnapshotModified()
  {
    return isModified(IMAGESNAPSHOT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBlobMdDTO getImageSnapshotMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBlobMdDTO) getAttributeDTO(IMAGESNAPSHOT).getAttributeMdDTO();
  }
  
  public String getKeyName()
  {
    return getValue(KEYNAME);
  }
  
  public void setKeyName(String value)
  {
    if(value == null)
    {
      setValue(KEYNAME, "");
    }
    else
    {
      setValue(KEYNAME, value);
    }
  }
  
  public boolean isKeyNameWritable()
  {
    return isWritable(KEYNAME);
  }
  
  public boolean isKeyNameReadable()
  {
    return isReadable(KEYNAME);
  }
  
  public boolean isKeyNameModified()
  {
    return isModified(KEYNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getKeyNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(KEYNAME).getAttributeMdDTO();
  }
  
  public Integer getLegendXPosition()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LEGENDXPOSITION));
  }
  
  public void setLegendXPosition(Integer value)
  {
    if(value == null)
    {
      setValue(LEGENDXPOSITION, "");
    }
    else
    {
      setValue(LEGENDXPOSITION, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLegendXPositionWritable()
  {
    return isWritable(LEGENDXPOSITION);
  }
  
  public boolean isLegendXPositionReadable()
  {
    return isReadable(LEGENDXPOSITION);
  }
  
  public boolean isLegendXPositionModified()
  {
    return isModified(LEGENDXPOSITION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLegendXPositionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LEGENDXPOSITION).getAttributeMdDTO();
  }
  
  public Integer getLegendYPosition()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LEGENDYPOSITION));
  }
  
  public void setLegendYPosition(Integer value)
  {
    if(value == null)
    {
      setValue(LEGENDYPOSITION, "");
    }
    else
    {
      setValue(LEGENDYPOSITION, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLegendYPositionWritable()
  {
    return isWritable(LEGENDYPOSITION);
  }
  
  public boolean isLegendYPositionReadable()
  {
    return isReadable(LEGENDYPOSITION);
  }
  
  public boolean isLegendYPositionModified()
  {
    return isModified(LEGENDYPOSITION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLegendYPositionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LEGENDYPOSITION).getAttributeMdDTO();
  }
  
  public String getSiteMaster()
  {
    return getValue(SITEMASTER);
  }
  
  public boolean isSiteMasterWritable()
  {
    return isWritable(SITEMASTER);
  }
  
  public boolean isSiteMasterReadable()
  {
    return isReadable(SITEMASTER);
  }
  
  public boolean isSiteMasterModified()
  {
    return isModified(SITEMASTER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getSiteMasterMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(SITEMASTER).getAttributeMdDTO();
  }
  
  public static DashboardLegendDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (DashboardLegendDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createStruct(this);
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
  
  public static net.geoprism.dashboard.DashboardLegendQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.dashboard.DashboardLegendQueryDTO) clientRequest.getAllInstances(net.geoprism.dashboard.DashboardLegendDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
}
