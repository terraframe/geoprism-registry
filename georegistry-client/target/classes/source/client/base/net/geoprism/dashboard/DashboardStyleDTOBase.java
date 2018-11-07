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

@com.runwaysdk.business.ClassSignature(hash = 1126241293)
public abstract class DashboardStyleDTOBase extends com.runwaysdk.business.BusinessDTO 
{
  public final static String CLASS = "net.geoprism.dashboard.DashboardStyle";
  private static final long serialVersionUID = 1126241293;
  
  protected DashboardStyleDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected DashboardStyleDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String BASICPOINTSIZE = "basicPointSize";
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String ENABLELABEL = "enableLabel";
  public static java.lang.String ENABLEVALUE = "enableValue";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String OID = "oid";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LABELCOLOR = "labelColor";
  public static java.lang.String LABELFONT = "labelFont";
  public static java.lang.String LABELHALO = "labelHalo";
  public static java.lang.String LABELHALOWIDTH = "labelHaloWidth";
  public static java.lang.String LABELSIZE = "labelSize";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LINEOPACITY = "lineOpacity";
  public static java.lang.String LINESTROKE = "lineStroke";
  public static java.lang.String LINESTROKECAP = "lineStrokeCap";
  public static java.lang.String LINESTROKEWIDTH = "lineStrokeWidth";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String NAME = "name";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String POINTFILL = "pointFill";
  public static java.lang.String POINTOPACITY = "pointOpacity";
  public static java.lang.String POINTROTATION = "pointRotation";
  public static java.lang.String POINTSTROKE = "pointStroke";
  public static java.lang.String POINTSTROKEOPACITY = "pointStrokeOpacity";
  public static java.lang.String POINTSTROKEWIDTH = "pointStrokeWidth";
  public static java.lang.String POINTWELLKNOWNNAME = "pointWellKnownName";
  public static java.lang.String POLYGONFILL = "polygonFill";
  public static java.lang.String POLYGONFILLOPACITY = "polygonFillOpacity";
  public static java.lang.String POLYGONSTROKE = "polygonStroke";
  public static java.lang.String POLYGONSTROKEOPACITY = "polygonStrokeOpacity";
  public static java.lang.String POLYGONSTROKEWIDTH = "polygonStrokeWidth";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TYPE = "type";
  public static java.lang.String VALUECOLOR = "valueColor";
  public static java.lang.String VALUEFONT = "valueFont";
  public static java.lang.String VALUEHALO = "valueHalo";
  public static java.lang.String VALUEHALOWIDTH = "valueHaloWidth";
  public static java.lang.String VALUESIZE = "valueSize";
  public Integer getBasicPointSize()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(BASICPOINTSIZE));
  }
  
  public void setBasicPointSize(Integer value)
  {
    if(value == null)
    {
      setValue(BASICPOINTSIZE, "");
    }
    else
    {
      setValue(BASICPOINTSIZE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isBasicPointSizeWritable()
  {
    return isWritable(BASICPOINTSIZE);
  }
  
  public boolean isBasicPointSizeReadable()
  {
    return isReadable(BASICPOINTSIZE);
  }
  
  public boolean isBasicPointSizeModified()
  {
    return isModified(BASICPOINTSIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getBasicPointSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(BASICPOINTSIZE).getAttributeMdDTO();
  }
  
  public java.util.Date getCreateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(CREATEDATE));
  }
  
  public boolean isCreateDateWritable()
  {
    return isWritable(CREATEDATE);
  }
  
  public boolean isCreateDateReadable()
  {
    return isReadable(CREATEDATE);
  }
  
  public boolean isCreateDateModified()
  {
    return isModified(CREATEDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getCreateDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(CREATEDATE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getCreatedBy()
  {
    if(getValue(CREATEDBY) == null || getValue(CREATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(CREATEDBY));
    }
  }
  
  public String getCreatedById()
  {
    return getValue(CREATEDBY);
  }
  
  public boolean isCreatedByWritable()
  {
    return isWritable(CREATEDBY);
  }
  
  public boolean isCreatedByReadable()
  {
    return isReadable(CREATEDBY);
  }
  
  public boolean isCreatedByModified()
  {
    return isModified(CREATEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getCreatedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(CREATEDBY).getAttributeMdDTO();
  }
  
  public Boolean getEnableLabel()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(ENABLELABEL));
  }
  
  public void setEnableLabel(Boolean value)
  {
    if(value == null)
    {
      setValue(ENABLELABEL, "");
    }
    else
    {
      setValue(ENABLELABEL, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isEnableLabelWritable()
  {
    return isWritable(ENABLELABEL);
  }
  
  public boolean isEnableLabelReadable()
  {
    return isReadable(ENABLELABEL);
  }
  
  public boolean isEnableLabelModified()
  {
    return isModified(ENABLELABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getEnableLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(ENABLELABEL).getAttributeMdDTO();
  }
  
  public Boolean getEnableValue()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(ENABLEVALUE));
  }
  
  public void setEnableValue(Boolean value)
  {
    if(value == null)
    {
      setValue(ENABLEVALUE, "");
    }
    else
    {
      setValue(ENABLEVALUE, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isEnableValueWritable()
  {
    return isWritable(ENABLEVALUE);
  }
  
  public boolean isEnableValueReadable()
  {
    return isReadable(ENABLEVALUE);
  }
  
  public boolean isEnableValueModified()
  {
    return isModified(ENABLEVALUE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getEnableValueMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(ENABLEVALUE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.metadata.MdDomainDTO getEntityDomain()
  {
    if(getValue(ENTITYDOMAIN) == null || getValue(ENTITYDOMAIN).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdDomainDTO.get(getRequest(), getValue(ENTITYDOMAIN));
    }
  }
  
  public String getEntityDomainId()
  {
    return getValue(ENTITYDOMAIN);
  }
  
  public void setEntityDomain(com.runwaysdk.system.metadata.MdDomainDTO value)
  {
    if(value == null)
    {
      setValue(ENTITYDOMAIN, "");
    }
    else
    {
      setValue(ENTITYDOMAIN, value.getOid());
    }
  }
  
  public boolean isEntityDomainWritable()
  {
    return isWritable(ENTITYDOMAIN);
  }
  
  public boolean isEntityDomainReadable()
  {
    return isReadable(ENTITYDOMAIN);
  }
  
  public boolean isEntityDomainModified()
  {
    return isModified(ENTITYDOMAIN);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getEntityDomainMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(ENTITYDOMAIN).getAttributeMdDTO();
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
  
  public String getLabelColor()
  {
    return getValue(LABELCOLOR);
  }
  
  public void setLabelColor(String value)
  {
    if(value == null)
    {
      setValue(LABELCOLOR, "");
    }
    else
    {
      setValue(LABELCOLOR, value);
    }
  }
  
  public boolean isLabelColorWritable()
  {
    return isWritable(LABELCOLOR);
  }
  
  public boolean isLabelColorReadable()
  {
    return isReadable(LABELCOLOR);
  }
  
  public boolean isLabelColorModified()
  {
    return isModified(LABELCOLOR);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getLabelColorMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LABELCOLOR).getAttributeMdDTO();
  }
  
  public String getLabelFont()
  {
    return getValue(LABELFONT);
  }
  
  public void setLabelFont(String value)
  {
    if(value == null)
    {
      setValue(LABELFONT, "");
    }
    else
    {
      setValue(LABELFONT, value);
    }
  }
  
  public boolean isLabelFontWritable()
  {
    return isWritable(LABELFONT);
  }
  
  public boolean isLabelFontReadable()
  {
    return isReadable(LABELFONT);
  }
  
  public boolean isLabelFontModified()
  {
    return isModified(LABELFONT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getLabelFontMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LABELFONT).getAttributeMdDTO();
  }
  
  public String getLabelHalo()
  {
    return getValue(LABELHALO);
  }
  
  public void setLabelHalo(String value)
  {
    if(value == null)
    {
      setValue(LABELHALO, "");
    }
    else
    {
      setValue(LABELHALO, value);
    }
  }
  
  public boolean isLabelHaloWritable()
  {
    return isWritable(LABELHALO);
  }
  
  public boolean isLabelHaloReadable()
  {
    return isReadable(LABELHALO);
  }
  
  public boolean isLabelHaloModified()
  {
    return isModified(LABELHALO);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getLabelHaloMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LABELHALO).getAttributeMdDTO();
  }
  
  public Integer getLabelHaloWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LABELHALOWIDTH));
  }
  
  public void setLabelHaloWidth(Integer value)
  {
    if(value == null)
    {
      setValue(LABELHALOWIDTH, "");
    }
    else
    {
      setValue(LABELHALOWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLabelHaloWidthWritable()
  {
    return isWritable(LABELHALOWIDTH);
  }
  
  public boolean isLabelHaloWidthReadable()
  {
    return isReadable(LABELHALOWIDTH);
  }
  
  public boolean isLabelHaloWidthModified()
  {
    return isModified(LABELHALOWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLabelHaloWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LABELHALOWIDTH).getAttributeMdDTO();
  }
  
  public Integer getLabelSize()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LABELSIZE));
  }
  
  public void setLabelSize(Integer value)
  {
    if(value == null)
    {
      setValue(LABELSIZE, "");
    }
    else
    {
      setValue(LABELSIZE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLabelSizeWritable()
  {
    return isWritable(LABELSIZE);
  }
  
  public boolean isLabelSizeReadable()
  {
    return isReadable(LABELSIZE);
  }
  
  public boolean isLabelSizeModified()
  {
    return isModified(LABELSIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLabelSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LABELSIZE).getAttributeMdDTO();
  }
  
  public java.util.Date getLastUpdateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(LASTUPDATEDATE));
  }
  
  public boolean isLastUpdateDateWritable()
  {
    return isWritable(LASTUPDATEDATE);
  }
  
  public boolean isLastUpdateDateReadable()
  {
    return isReadable(LASTUPDATEDATE);
  }
  
  public boolean isLastUpdateDateModified()
  {
    return isModified(LASTUPDATEDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getLastUpdateDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(LASTUPDATEDATE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getLastUpdatedBy()
  {
    if(getValue(LASTUPDATEDBY) == null || getValue(LASTUPDATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(LASTUPDATEDBY));
    }
  }
  
  public String getLastUpdatedById()
  {
    return getValue(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByWritable()
  {
    return isWritable(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByReadable()
  {
    return isReadable(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByModified()
  {
    return isModified(LASTUPDATEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getLastUpdatedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(LASTUPDATEDBY).getAttributeMdDTO();
  }
  
  public Double getLineOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(LINEOPACITY));
  }
  
  public void setLineOpacity(Double value)
  {
    if(value == null)
    {
      setValue(LINEOPACITY, "");
    }
    else
    {
      setValue(LINEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isLineOpacityWritable()
  {
    return isWritable(LINEOPACITY);
  }
  
  public boolean isLineOpacityReadable()
  {
    return isReadable(LINEOPACITY);
  }
  
  public boolean isLineOpacityModified()
  {
    return isModified(LINEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getLineOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(LINEOPACITY).getAttributeMdDTO();
  }
  
  public String getLineStroke()
  {
    return getValue(LINESTROKE);
  }
  
  public void setLineStroke(String value)
  {
    if(value == null)
    {
      setValue(LINESTROKE, "");
    }
    else
    {
      setValue(LINESTROKE, value);
    }
  }
  
  public boolean isLineStrokeWritable()
  {
    return isWritable(LINESTROKE);
  }
  
  public boolean isLineStrokeReadable()
  {
    return isReadable(LINESTROKE);
  }
  
  public boolean isLineStrokeModified()
  {
    return isModified(LINESTROKE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getLineStrokeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LINESTROKE).getAttributeMdDTO();
  }
  
  public String getLineStrokeCap()
  {
    return getValue(LINESTROKECAP);
  }
  
  public void setLineStrokeCap(String value)
  {
    if(value == null)
    {
      setValue(LINESTROKECAP, "");
    }
    else
    {
      setValue(LINESTROKECAP, value);
    }
  }
  
  public boolean isLineStrokeCapWritable()
  {
    return isWritable(LINESTROKECAP);
  }
  
  public boolean isLineStrokeCapReadable()
  {
    return isReadable(LINESTROKECAP);
  }
  
  public boolean isLineStrokeCapModified()
  {
    return isModified(LINESTROKECAP);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getLineStrokeCapMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LINESTROKECAP).getAttributeMdDTO();
  }
  
  public Integer getLineStrokeWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LINESTROKEWIDTH));
  }
  
  public void setLineStrokeWidth(Integer value)
  {
    if(value == null)
    {
      setValue(LINESTROKEWIDTH, "");
    }
    else
    {
      setValue(LINESTROKEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLineStrokeWidthWritable()
  {
    return isWritable(LINESTROKEWIDTH);
  }
  
  public boolean isLineStrokeWidthReadable()
  {
    return isReadable(LINESTROKEWIDTH);
  }
  
  public boolean isLineStrokeWidthModified()
  {
    return isModified(LINESTROKEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLineStrokeWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LINESTROKEWIDTH).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getLockedBy()
  {
    if(getValue(LOCKEDBY) == null || getValue(LOCKEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(LOCKEDBY));
    }
  }
  
  public String getLockedById()
  {
    return getValue(LOCKEDBY);
  }
  
  public boolean isLockedByWritable()
  {
    return isWritable(LOCKEDBY);
  }
  
  public boolean isLockedByReadable()
  {
    return isReadable(LOCKEDBY);
  }
  
  public boolean isLockedByModified()
  {
    return isModified(LOCKEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getLockedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(LOCKEDBY).getAttributeMdDTO();
  }
  
  public String getName()
  {
    return getValue(NAME);
  }
  
  public void setName(String value)
  {
    if(value == null)
    {
      setValue(NAME, "");
    }
    else
    {
      setValue(NAME, value);
    }
  }
  
  public boolean isNameWritable()
  {
    return isWritable(NAME);
  }
  
  public boolean isNameReadable()
  {
    return isReadable(NAME);
  }
  
  public boolean isNameModified()
  {
    return isModified(NAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(NAME).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.ActorDTO getOwner()
  {
    if(getValue(OWNER) == null || getValue(OWNER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.ActorDTO.get(getRequest(), getValue(OWNER));
    }
  }
  
  public String getOwnerId()
  {
    return getValue(OWNER);
  }
  
  public void setOwner(com.runwaysdk.system.ActorDTO value)
  {
    if(value == null)
    {
      setValue(OWNER, "");
    }
    else
    {
      setValue(OWNER, value.getOid());
    }
  }
  
  public boolean isOwnerWritable()
  {
    return isWritable(OWNER);
  }
  
  public boolean isOwnerReadable()
  {
    return isReadable(OWNER);
  }
  
  public boolean isOwnerModified()
  {
    return isModified(OWNER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getOwnerMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(OWNER).getAttributeMdDTO();
  }
  
  public String getPointFill()
  {
    return getValue(POINTFILL);
  }
  
  public void setPointFill(String value)
  {
    if(value == null)
    {
      setValue(POINTFILL, "");
    }
    else
    {
      setValue(POINTFILL, value);
    }
  }
  
  public boolean isPointFillWritable()
  {
    return isWritable(POINTFILL);
  }
  
  public boolean isPointFillReadable()
  {
    return isReadable(POINTFILL);
  }
  
  public boolean isPointFillModified()
  {
    return isModified(POINTFILL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getPointFillMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(POINTFILL).getAttributeMdDTO();
  }
  
  public Double getPointOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(POINTOPACITY));
  }
  
  public void setPointOpacity(Double value)
  {
    if(value == null)
    {
      setValue(POINTOPACITY, "");
    }
    else
    {
      setValue(POINTOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isPointOpacityWritable()
  {
    return isWritable(POINTOPACITY);
  }
  
  public boolean isPointOpacityReadable()
  {
    return isReadable(POINTOPACITY);
  }
  
  public boolean isPointOpacityModified()
  {
    return isModified(POINTOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getPointOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(POINTOPACITY).getAttributeMdDTO();
  }
  
  public Integer getPointRotation()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(POINTROTATION));
  }
  
  public void setPointRotation(Integer value)
  {
    if(value == null)
    {
      setValue(POINTROTATION, "");
    }
    else
    {
      setValue(POINTROTATION, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isPointRotationWritable()
  {
    return isWritable(POINTROTATION);
  }
  
  public boolean isPointRotationReadable()
  {
    return isReadable(POINTROTATION);
  }
  
  public boolean isPointRotationModified()
  {
    return isModified(POINTROTATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getPointRotationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(POINTROTATION).getAttributeMdDTO();
  }
  
  public String getPointStroke()
  {
    return getValue(POINTSTROKE);
  }
  
  public void setPointStroke(String value)
  {
    if(value == null)
    {
      setValue(POINTSTROKE, "");
    }
    else
    {
      setValue(POINTSTROKE, value);
    }
  }
  
  public boolean isPointStrokeWritable()
  {
    return isWritable(POINTSTROKE);
  }
  
  public boolean isPointStrokeReadable()
  {
    return isReadable(POINTSTROKE);
  }
  
  public boolean isPointStrokeModified()
  {
    return isModified(POINTSTROKE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getPointStrokeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(POINTSTROKE).getAttributeMdDTO();
  }
  
  public Double getPointStrokeOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(POINTSTROKEOPACITY));
  }
  
  public void setPointStrokeOpacity(Double value)
  {
    if(value == null)
    {
      setValue(POINTSTROKEOPACITY, "");
    }
    else
    {
      setValue(POINTSTROKEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isPointStrokeOpacityWritable()
  {
    return isWritable(POINTSTROKEOPACITY);
  }
  
  public boolean isPointStrokeOpacityReadable()
  {
    return isReadable(POINTSTROKEOPACITY);
  }
  
  public boolean isPointStrokeOpacityModified()
  {
    return isModified(POINTSTROKEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getPointStrokeOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(POINTSTROKEOPACITY).getAttributeMdDTO();
  }
  
  public Integer getPointStrokeWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(POINTSTROKEWIDTH));
  }
  
  public void setPointStrokeWidth(Integer value)
  {
    if(value == null)
    {
      setValue(POINTSTROKEWIDTH, "");
    }
    else
    {
      setValue(POINTSTROKEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isPointStrokeWidthWritable()
  {
    return isWritable(POINTSTROKEWIDTH);
  }
  
  public boolean isPointStrokeWidthReadable()
  {
    return isReadable(POINTSTROKEWIDTH);
  }
  
  public boolean isPointStrokeWidthModified()
  {
    return isModified(POINTSTROKEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getPointStrokeWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(POINTSTROKEWIDTH).getAttributeMdDTO();
  }
  
  public String getPointWellKnownName()
  {
    return getValue(POINTWELLKNOWNNAME);
  }
  
  public void setPointWellKnownName(String value)
  {
    if(value == null)
    {
      setValue(POINTWELLKNOWNNAME, "");
    }
    else
    {
      setValue(POINTWELLKNOWNNAME, value);
    }
  }
  
  public boolean isPointWellKnownNameWritable()
  {
    return isWritable(POINTWELLKNOWNNAME);
  }
  
  public boolean isPointWellKnownNameReadable()
  {
    return isReadable(POINTWELLKNOWNNAME);
  }
  
  public boolean isPointWellKnownNameModified()
  {
    return isModified(POINTWELLKNOWNNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getPointWellKnownNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(POINTWELLKNOWNNAME).getAttributeMdDTO();
  }
  
  public String getPolygonFill()
  {
    return getValue(POLYGONFILL);
  }
  
  public void setPolygonFill(String value)
  {
    if(value == null)
    {
      setValue(POLYGONFILL, "");
    }
    else
    {
      setValue(POLYGONFILL, value);
    }
  }
  
  public boolean isPolygonFillWritable()
  {
    return isWritable(POLYGONFILL);
  }
  
  public boolean isPolygonFillReadable()
  {
    return isReadable(POLYGONFILL);
  }
  
  public boolean isPolygonFillModified()
  {
    return isModified(POLYGONFILL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getPolygonFillMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(POLYGONFILL).getAttributeMdDTO();
  }
  
  public Double getPolygonFillOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(POLYGONFILLOPACITY));
  }
  
  public void setPolygonFillOpacity(Double value)
  {
    if(value == null)
    {
      setValue(POLYGONFILLOPACITY, "");
    }
    else
    {
      setValue(POLYGONFILLOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isPolygonFillOpacityWritable()
  {
    return isWritable(POLYGONFILLOPACITY);
  }
  
  public boolean isPolygonFillOpacityReadable()
  {
    return isReadable(POLYGONFILLOPACITY);
  }
  
  public boolean isPolygonFillOpacityModified()
  {
    return isModified(POLYGONFILLOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getPolygonFillOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(POLYGONFILLOPACITY).getAttributeMdDTO();
  }
  
  public String getPolygonStroke()
  {
    return getValue(POLYGONSTROKE);
  }
  
  public void setPolygonStroke(String value)
  {
    if(value == null)
    {
      setValue(POLYGONSTROKE, "");
    }
    else
    {
      setValue(POLYGONSTROKE, value);
    }
  }
  
  public boolean isPolygonStrokeWritable()
  {
    return isWritable(POLYGONSTROKE);
  }
  
  public boolean isPolygonStrokeReadable()
  {
    return isReadable(POLYGONSTROKE);
  }
  
  public boolean isPolygonStrokeModified()
  {
    return isModified(POLYGONSTROKE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getPolygonStrokeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(POLYGONSTROKE).getAttributeMdDTO();
  }
  
  public Double getPolygonStrokeOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(POLYGONSTROKEOPACITY));
  }
  
  public void setPolygonStrokeOpacity(Double value)
  {
    if(value == null)
    {
      setValue(POLYGONSTROKEOPACITY, "");
    }
    else
    {
      setValue(POLYGONSTROKEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isPolygonStrokeOpacityWritable()
  {
    return isWritable(POLYGONSTROKEOPACITY);
  }
  
  public boolean isPolygonStrokeOpacityReadable()
  {
    return isReadable(POLYGONSTROKEOPACITY);
  }
  
  public boolean isPolygonStrokeOpacityModified()
  {
    return isModified(POLYGONSTROKEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getPolygonStrokeOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(POLYGONSTROKEOPACITY).getAttributeMdDTO();
  }
  
  public Integer getPolygonStrokeWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(POLYGONSTROKEWIDTH));
  }
  
  public void setPolygonStrokeWidth(Integer value)
  {
    if(value == null)
    {
      setValue(POLYGONSTROKEWIDTH, "");
    }
    else
    {
      setValue(POLYGONSTROKEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isPolygonStrokeWidthWritable()
  {
    return isWritable(POLYGONSTROKEWIDTH);
  }
  
  public boolean isPolygonStrokeWidthReadable()
  {
    return isReadable(POLYGONSTROKEWIDTH);
  }
  
  public boolean isPolygonStrokeWidthModified()
  {
    return isModified(POLYGONSTROKEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getPolygonStrokeWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(POLYGONSTROKEWIDTH).getAttributeMdDTO();
  }
  
  public Long getSeq()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(SEQ));
  }
  
  public boolean isSeqWritable()
  {
    return isWritable(SEQ);
  }
  
  public boolean isSeqReadable()
  {
    return isReadable(SEQ);
  }
  
  public boolean isSeqModified()
  {
    return isModified(SEQ);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getSeqMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(SEQ).getAttributeMdDTO();
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
  
  public String getValueColor()
  {
    return getValue(VALUECOLOR);
  }
  
  public void setValueColor(String value)
  {
    if(value == null)
    {
      setValue(VALUECOLOR, "");
    }
    else
    {
      setValue(VALUECOLOR, value);
    }
  }
  
  public boolean isValueColorWritable()
  {
    return isWritable(VALUECOLOR);
  }
  
  public boolean isValueColorReadable()
  {
    return isReadable(VALUECOLOR);
  }
  
  public boolean isValueColorModified()
  {
    return isModified(VALUECOLOR);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getValueColorMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(VALUECOLOR).getAttributeMdDTO();
  }
  
  public String getValueFont()
  {
    return getValue(VALUEFONT);
  }
  
  public void setValueFont(String value)
  {
    if(value == null)
    {
      setValue(VALUEFONT, "");
    }
    else
    {
      setValue(VALUEFONT, value);
    }
  }
  
  public boolean isValueFontWritable()
  {
    return isWritable(VALUEFONT);
  }
  
  public boolean isValueFontReadable()
  {
    return isReadable(VALUEFONT);
  }
  
  public boolean isValueFontModified()
  {
    return isModified(VALUEFONT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getValueFontMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(VALUEFONT).getAttributeMdDTO();
  }
  
  public String getValueHalo()
  {
    return getValue(VALUEHALO);
  }
  
  public void setValueHalo(String value)
  {
    if(value == null)
    {
      setValue(VALUEHALO, "");
    }
    else
    {
      setValue(VALUEHALO, value);
    }
  }
  
  public boolean isValueHaloWritable()
  {
    return isWritable(VALUEHALO);
  }
  
  public boolean isValueHaloReadable()
  {
    return isReadable(VALUEHALO);
  }
  
  public boolean isValueHaloModified()
  {
    return isModified(VALUEHALO);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getValueHaloMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(VALUEHALO).getAttributeMdDTO();
  }
  
  public Integer getValueHaloWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(VALUEHALOWIDTH));
  }
  
  public void setValueHaloWidth(Integer value)
  {
    if(value == null)
    {
      setValue(VALUEHALOWIDTH, "");
    }
    else
    {
      setValue(VALUEHALOWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isValueHaloWidthWritable()
  {
    return isWritable(VALUEHALOWIDTH);
  }
  
  public boolean isValueHaloWidthReadable()
  {
    return isReadable(VALUEHALOWIDTH);
  }
  
  public boolean isValueHaloWidthModified()
  {
    return isModified(VALUEHALOWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getValueHaloWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(VALUEHALOWIDTH).getAttributeMdDTO();
  }
  
  public Integer getValueSize()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(VALUESIZE));
  }
  
  public void setValueSize(Integer value)
  {
    if(value == null)
    {
      setValue(VALUESIZE, "");
    }
    else
    {
      setValue(VALUESIZE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isValueSizeWritable()
  {
    return isWritable(VALUESIZE);
  }
  
  public boolean isValueSizeReadable()
  {
    return isReadable(VALUESIZE);
  }
  
  public boolean isValueSizeModified()
  {
    return isModified(VALUESIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getValueSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(VALUESIZE).getAttributeMdDTO();
  }
  
  public static final java.lang.String getAggregationJSON(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardStyleDTO.CLASS, "getAggregationJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getJSON()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardStyleDTO.CLASS, "getJSON", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getJSON(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardStyleDTO.CLASS, "getJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.dashboard.AggregationTypeQueryDTO getSortedAggregations(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String thematicAttributeId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{thematicAttributeId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardStyleDTO.CLASS, "getSortedAggregations", _declaredTypes);
    return (net.geoprism.dashboard.AggregationTypeQueryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String[] getSortedFonts(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardStyleDTO.CLASS, "getSortedFonts", _declaredTypes);
    return (java.lang.String[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.dashboard.layer.DashboardLayerDTO> getAllContainingLayer()
  {
    return (java.util.List<? extends net.geoprism.dashboard.layer.DashboardLayerDTO>) getRequest().getParents(this.getOid(), net.geoprism.dashboard.HasStyleDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.dashboard.layer.DashboardLayerDTO> getAllContainingLayer(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.dashboard.layer.DashboardLayerDTO>) clientRequestIF.getParents(oid, net.geoprism.dashboard.HasStyleDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.dashboard.HasStyleDTO> getAllContainingLayerRelationships()
  {
    return (java.util.List<? extends net.geoprism.dashboard.HasStyleDTO>) getRequest().getParentRelationships(this.getOid(), net.geoprism.dashboard.HasStyleDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.dashboard.HasStyleDTO> getAllContainingLayerRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.dashboard.HasStyleDTO>) clientRequestIF.getParentRelationships(oid, net.geoprism.dashboard.HasStyleDTO.CLASS);
  }
  
  public net.geoprism.dashboard.HasStyleDTO addContainingLayer(net.geoprism.dashboard.layer.DashboardLayerDTO parent)
  {
    return (net.geoprism.dashboard.HasStyleDTO) getRequest().addParent(parent.getOid(), this.getOid(), net.geoprism.dashboard.HasStyleDTO.CLASS);
  }
  
  public static net.geoprism.dashboard.HasStyleDTO addContainingLayer(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, net.geoprism.dashboard.layer.DashboardLayerDTO parent)
  {
    return (net.geoprism.dashboard.HasStyleDTO) clientRequestIF.addParent(parent.getOid(), oid, net.geoprism.dashboard.HasStyleDTO.CLASS);
  }
  
  public void removeContainingLayer(net.geoprism.dashboard.HasStyleDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeContainingLayer(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.dashboard.HasStyleDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllContainingLayer()
  {
    getRequest().deleteParents(this.getOid(), net.geoprism.dashboard.HasStyleDTO.CLASS);
  }
  
  public static void removeAllContainingLayer(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, net.geoprism.dashboard.HasStyleDTO.CLASS);
  }
  
  public static net.geoprism.dashboard.DashboardStyleDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.dashboard.DashboardStyleDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createBusiness(this);
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
  
  public static net.geoprism.dashboard.DashboardStyleQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.dashboard.DashboardStyleQueryDTO) clientRequest.getAllInstances(net.geoprism.dashboard.DashboardStyleDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.dashboard.DashboardStyleDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardStyleDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.dashboard.DashboardStyleDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.dashboard.DashboardStyleDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardStyleDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.dashboard.DashboardStyleDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
