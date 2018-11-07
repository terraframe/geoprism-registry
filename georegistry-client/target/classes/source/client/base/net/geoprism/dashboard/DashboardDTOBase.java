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

@com.runwaysdk.business.ClassSignature(hash = -602530566)
public abstract class DashboardDTOBase extends com.runwaysdk.business.BusinessDTO 
{
  public final static String CLASS = "net.geoprism.dashboard.Dashboard";
  private static final long serialVersionUID = -602530566;
  
  protected DashboardDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected DashboardDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String DASHBOARDROLE = "dashboardRole";
  public static java.lang.String DESCRIPTION = "description";
  public static java.lang.String DISPLAYLABEL = "displayLabel";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String FILTERDATE = "filterDate";
  public static java.lang.String FROMDATE = "fromDate";
  public static java.lang.String OID = "oid";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String NAME = "name";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String REMOVABLE = "removable";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TODATE = "toDate";
  public static java.lang.String TYPE = "type";
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
  
  public com.runwaysdk.system.RolesDTO getDashboardRole()
  {
    if(getValue(DASHBOARDROLE) == null || getValue(DASHBOARDROLE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.RolesDTO.get(getRequest(), getValue(DASHBOARDROLE));
    }
  }
  
  public String getDashboardRoleId()
  {
    return getValue(DASHBOARDROLE);
  }
  
  public void setDashboardRole(com.runwaysdk.system.RolesDTO value)
  {
    if(value == null)
    {
      setValue(DASHBOARDROLE, "");
    }
    else
    {
      setValue(DASHBOARDROLE, value.getOid());
    }
  }
  
  public boolean isDashboardRoleWritable()
  {
    return isWritable(DASHBOARDROLE);
  }
  
  public boolean isDashboardRoleReadable()
  {
    return isReadable(DASHBOARDROLE);
  }
  
  public boolean isDashboardRoleModified()
  {
    return isModified(DASHBOARDROLE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getDashboardRoleMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(DASHBOARDROLE).getAttributeMdDTO();
  }
  
  public net.geoprism.dashboard.DashboardDescriptionDTO getDescription()
  {
    return (net.geoprism.dashboard.DashboardDescriptionDTO) this.getAttributeStructDTO(DESCRIPTION).getStructDTO();
  }
  
  public boolean isDescriptionWritable()
  {
    return isWritable(DESCRIPTION);
  }
  
  public boolean isDescriptionReadable()
  {
    return isReadable(DESCRIPTION);
  }
  
  public boolean isDescriptionModified()
  {
    return isModified(DESCRIPTION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalCharacterMdDTO getDescriptionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalCharacterMdDTO) getAttributeDTO(DESCRIPTION).getAttributeMdDTO();
  }
  
  public net.geoprism.dashboard.DashboardDisplayLabelDTO getDisplayLabel()
  {
    return (net.geoprism.dashboard.DashboardDisplayLabelDTO) this.getAttributeStructDTO(DISPLAYLABEL).getStructDTO();
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
  
  public final com.runwaysdk.transport.metadata.AttributeLocalCharacterMdDTO getDisplayLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalCharacterMdDTO) getAttributeDTO(DISPLAYLABEL).getAttributeMdDTO();
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
  
  public com.runwaysdk.system.metadata.MdAttributeDTO getFilterDate()
  {
    if(getValue(FILTERDATE) == null || getValue(FILTERDATE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdAttributeDTO.get(getRequest(), getValue(FILTERDATE));
    }
  }
  
  public String getFilterDateId()
  {
    return getValue(FILTERDATE);
  }
  
  public void setFilterDate(com.runwaysdk.system.metadata.MdAttributeDTO value)
  {
    if(value == null)
    {
      setValue(FILTERDATE, "");
    }
    else
    {
      setValue(FILTERDATE, value.getOid());
    }
  }
  
  public boolean isFilterDateWritable()
  {
    return isWritable(FILTERDATE);
  }
  
  public boolean isFilterDateReadable()
  {
    return isReadable(FILTERDATE);
  }
  
  public boolean isFilterDateModified()
  {
    return isModified(FILTERDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getFilterDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(FILTERDATE).getAttributeMdDTO();
  }
  
  public java.util.Date getFromDate()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(FROMDATE));
  }
  
  public void setFromDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(FROMDATE, "");
    }
    else
    {
      setValue(FROMDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATE_FORMAT).format(value));
    }
  }
  
  public boolean isFromDateWritable()
  {
    return isWritable(FROMDATE);
  }
  
  public boolean isFromDateReadable()
  {
    return isReadable(FROMDATE);
  }
  
  public boolean isFromDateModified()
  {
    return isModified(FROMDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateMdDTO getFromDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateMdDTO) getAttributeDTO(FROMDATE).getAttributeMdDTO();
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
  
  public Boolean getRemovable()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(REMOVABLE));
  }
  
  public void setRemovable(Boolean value)
  {
    if(value == null)
    {
      setValue(REMOVABLE, "");
    }
    else
    {
      setValue(REMOVABLE, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isRemovableWritable()
  {
    return isWritable(REMOVABLE);
  }
  
  public boolean isRemovableReadable()
  {
    return isReadable(REMOVABLE);
  }
  
  public boolean isRemovableModified()
  {
    return isModified(REMOVABLE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getRemovableMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(REMOVABLE).getAttributeMdDTO();
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
  
  public java.util.Date getToDate()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(TODATE));
  }
  
  public void setToDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(TODATE, "");
    }
    else
    {
      setValue(TODATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATE_FORMAT).format(value));
    }
  }
  
  public boolean isToDateWritable()
  {
    return isWritable(TODATE);
  }
  
  public boolean isToDateReadable()
  {
    return isReadable(TODATE);
  }
  
  public boolean isToDateModified()
  {
    return isModified(TODATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateMdDTO getToDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateMdDTO) getAttributeDTO(TODATE).getAttributeMdDTO();
  }
  
  public final java.lang.String applyWithOptions(java.lang.String options)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{options};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "applyWithOptions", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String applyWithOptions(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String options)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{oid, options};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "applyWithOptions", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void assignUsers(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String dashboardId, java.lang.String[] userIds)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "[Ljava.lang.String;"};
    Object[] _parameters = new Object[]{dashboardId, userIds};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "assignUsers", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final net.geoprism.dashboard.DashboardDTO clone(java.lang.String name)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{name};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "clone", _declaredTypes);
    return (net.geoprism.dashboard.DashboardDTO) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final net.geoprism.dashboard.DashboardDTO clone(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String name)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{oid, name};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "clone", _declaredTypes);
    return (net.geoprism.dashboard.DashboardDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.dashboard.DashboardDTO create(com.runwaysdk.constants.ClientRequestIF clientRequest, net.geoprism.dashboard.DashboardDTO dto)
  {
    String[] _declaredTypes = new String[]{"net.geoprism.dashboard.Dashboard"};
    Object[] _parameters = new Object[]{dto};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "create", _declaredTypes);
    return (net.geoprism.dashboard.DashboardDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final void generateThumbnailImage()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "generateThumbnailImage", _declaredTypes);
    getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final void generateThumbnailImage(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "generateThumbnailImage", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final net.geoprism.GeoprismUserDTO[] getAllDashboardUsers()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getAllDashboardUsers", _declaredTypes);
    return (net.geoprism.GeoprismUserDTO[]) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final net.geoprism.GeoprismUserDTO[] getAllDashboardUsers(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getAllDashboardUsers", _declaredTypes);
    return (net.geoprism.GeoprismUserDTO[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getAllDashboardUsersJSON()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getAllDashboardUsersJSON", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getAllDashboardUsersJSON(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getAllDashboardUsersJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String getAvailableDashboardsAsJSON(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String dashboardId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{dashboardId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getAvailableDashboardsAsJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String[] getCategoryInputSuggestions(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String mdAttributeId, java.lang.String geoNodeId, java.lang.String universalId, java.lang.String aggregationVal, java.lang.String text, java.lang.Integer limit, java.lang.String state)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "java.lang.Integer", "java.lang.String"};
    Object[] _parameters = new Object[]{mdAttributeId, geoNodeId, universalId, aggregationVal, text, limit, state};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getCategoryInputSuggestions", _declaredTypes);
    return (java.lang.String[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.ontology.ClassifierDTO[] getClassifierRoots(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String mdAttributeId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{mdAttributeId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getClassifierRoots", _declaredTypes);
    return (net.geoprism.ontology.ClassifierDTO[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.ontology.ClassifierDTO[] getClassifierSuggestions(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String mdAttributeId, java.lang.String text, java.lang.Integer limit)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.Integer"};
    Object[] _parameters = new Object[]{mdAttributeId, text, limit};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getClassifierSuggestions", _declaredTypes);
    return (net.geoprism.ontology.ClassifierDTO[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String getClassifierTree(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String mdAttributeId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{mdAttributeId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getClassifierTree", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getConditionsJSON()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getConditionsJSON", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getConditionsJSON(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getConditionsJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getDashboardDefinition()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getDashboardDefinition", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getDashboardDefinition(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getDashboardDefinition", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getDashboardInformation()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getDashboardInformation", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getDashboardInformation(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getDashboardInformation", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final com.runwaysdk.business.ValueQueryDTO getGeoEntitySuggestions(java.lang.String text, java.lang.Integer limit)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.Integer"};
    Object[] _parameters = new Object[]{text, limit};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getGeoEntitySuggestions", _declaredTypes);
    return (com.runwaysdk.business.ValueQueryDTO) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final com.runwaysdk.business.ValueQueryDTO getGeoEntitySuggestions(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String text, java.lang.Integer limit)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.Integer"};
    Object[] _parameters = new Object[]{oid, text, limit};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getGeoEntitySuggestions", _declaredTypes);
    return (com.runwaysdk.business.ValueQueryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final com.runwaysdk.system.gis.geo.GeoNodeDTO[] getGeoNodes(com.runwaysdk.system.metadata.MdAttributeDTO thematicAttribute)
  {
    String[] _declaredTypes = new String[]{"com.runwaysdk.system.metadata.MdAttribute"};
    Object[] _parameters = new Object[]{thematicAttribute};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getGeoNodes", _declaredTypes);
    return (com.runwaysdk.system.gis.geo.GeoNodeDTO[]) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final com.runwaysdk.system.gis.geo.GeoNodeDTO[] getGeoNodes(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, com.runwaysdk.system.metadata.MdAttributeDTO thematicAttribute)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "com.runwaysdk.system.metadata.MdAttribute"};
    Object[] _parameters = new Object[]{oid, thematicAttribute};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getGeoNodes", _declaredTypes);
    return (com.runwaysdk.system.gis.geo.GeoNodeDTO[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getGeoNodesJSON(com.runwaysdk.system.metadata.MdAttributeDTO thematicAttribute)
  {
    String[] _declaredTypes = new String[]{"com.runwaysdk.system.metadata.MdAttribute"};
    Object[] _parameters = new Object[]{thematicAttribute};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getGeoNodesJSON", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getGeoNodesJSON(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, com.runwaysdk.system.metadata.MdAttributeDTO thematicAttribute)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "com.runwaysdk.system.metadata.MdAttribute"};
    Object[] _parameters = new Object[]{oid, thematicAttribute};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getGeoNodesJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getJSON()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getJSON", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getJSON(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getLayersToDelete(java.lang.String options)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{options};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getLayersToDelete", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getLayersToDelete(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String options)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{oid, options};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getLayersToDelete", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getMapId()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getMapId", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getMapId(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getMapId", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.dashboard.DashboardQueryDTO getSortedDashboards(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getSortedDashboards", _declaredTypes);
    return (net.geoprism.dashboard.DashboardQueryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final com.runwaysdk.system.metadata.MdClassDTO[] getSortedTypes()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getSortedTypes", _declaredTypes);
    return (com.runwaysdk.system.metadata.MdClassDTO[]) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final com.runwaysdk.system.metadata.MdClassDTO[] getSortedTypes(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getSortedTypes", _declaredTypes);
    return (com.runwaysdk.system.metadata.MdClassDTO[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String[] getTextSuggestions(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String mdAttributeId, java.lang.String text, java.lang.Integer limit)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.Integer"};
    Object[] _parameters = new Object[]{mdAttributeId, text, limit};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getTextSuggestions", _declaredTypes);
    return (java.lang.String[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.io.InputStream getThumbnailStream()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getThumbnailStream", _declaredTypes);
    return (java.io.InputStream) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.io.InputStream getThumbnailStream(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "getThumbnailStream", _declaredTypes);
    return (java.io.InputStream) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.Boolean hasAccess()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "hasAccess", _declaredTypes);
    return (java.lang.Boolean) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.Boolean hasAccess(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "hasAccess", _declaredTypes);
    return (java.lang.Boolean) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.Boolean hasReport()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "hasReport", _declaredTypes);
    return (java.lang.Boolean) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.Boolean hasReport(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "hasReport", _declaredTypes);
    return (java.lang.Boolean) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String saveState(java.lang.String state, java.lang.Boolean global)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.Boolean"};
    Object[] _parameters = new Object[]{state, global};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "saveState", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String saveState(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String state, java.lang.Boolean global)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.Boolean"};
    Object[] _parameters = new Object[]{oid, state, global};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "saveState", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final void setBaseLayerState(java.lang.String baseLayerState)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{baseLayerState};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "setBaseLayerState", _declaredTypes);
    getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final void setBaseLayerState(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String baseLayerState)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{oid, baseLayerState};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "setBaseLayerState", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final void setDashboardAttributesOrder(java.lang.String typeId, java.lang.String[] attributeIds)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "[Ljava.lang.String;"};
    Object[] _parameters = new Object[]{typeId, attributeIds};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "setDashboardAttributesOrder", _declaredTypes);
    getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final void setDashboardAttributesOrder(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String typeId, java.lang.String[] attributeIds)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "[Ljava.lang.String;"};
    Object[] _parameters = new Object[]{oid, typeId, attributeIds};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "setDashboardAttributesOrder", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final void setMetadataWrapperOrder(java.lang.String[] typeIds)
  {
    String[] _declaredTypes = new String[]{"[Ljava.lang.String;"};
    Object[] _parameters = new Object[]{typeIds};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "setMetadataWrapperOrder", _declaredTypes);
    getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final void setMetadataWrapperOrder(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String[] typeIds)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "[Ljava.lang.String;"};
    Object[] _parameters = new Object[]{oid, typeIds};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "setMetadataWrapperOrder", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.dashboard.MetadataWrapperDTO> getAllMetadata()
  {
    return (java.util.List<? extends net.geoprism.dashboard.MetadataWrapperDTO>) getRequest().getChildren(this.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.dashboard.MetadataWrapperDTO> getAllMetadata(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.dashboard.MetadataWrapperDTO>) clientRequestIF.getChildren(oid, net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.dashboard.DashboardMetadataDTO> getAllMetadataRelationships()
  {
    return (java.util.List<? extends net.geoprism.dashboard.DashboardMetadataDTO>) getRequest().getChildRelationships(this.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.dashboard.DashboardMetadataDTO> getAllMetadataRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.dashboard.DashboardMetadataDTO>) clientRequestIF.getChildRelationships(oid, net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public net.geoprism.dashboard.DashboardMetadataDTO addMetadata(net.geoprism.dashboard.MetadataWrapperDTO child)
  {
    return (net.geoprism.dashboard.DashboardMetadataDTO) getRequest().addChild(this.getOid(), child.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public static net.geoprism.dashboard.DashboardMetadataDTO addMetadata(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, net.geoprism.dashboard.MetadataWrapperDTO child)
  {
    return (net.geoprism.dashboard.DashboardMetadataDTO) clientRequestIF.addChild(oid, child.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public void removeMetadata(net.geoprism.dashboard.DashboardMetadataDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeMetadata(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.dashboard.DashboardMetadataDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllMetadata()
  {
    getRequest().deleteChildren(this.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public static void removeAllMetadata(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public static net.geoprism.dashboard.DashboardDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.dashboard.DashboardDTO) dto;
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
  
  public static net.geoprism.dashboard.DashboardQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.dashboard.DashboardQueryDTO) clientRequest.getAllInstances(net.geoprism.dashboard.DashboardDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.dashboard.DashboardDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.dashboard.DashboardDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.dashboard.DashboardDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.dashboard.DashboardDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
