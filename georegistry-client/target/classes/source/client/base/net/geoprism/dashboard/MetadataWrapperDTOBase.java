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

@com.runwaysdk.business.ClassSignature(hash = -334095162)
public abstract class MetadataWrapperDTOBase extends com.runwaysdk.business.BusinessDTO 
{
  public final static String CLASS = "net.geoprism.dashboard.MetadataWrapper";
  private static final long serialVersionUID = -334095162;
  
  protected MetadataWrapperDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected MetadataWrapperDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String DASHBOARD = "dashboard";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String OID = "oid";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TYPE = "type";
  public static java.lang.String WRAPPEDMDCLASS = "wrappedMdClass";
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
  
  public net.geoprism.dashboard.DashboardDTO getDashboard()
  {
    if(getValue(DASHBOARD) == null || getValue(DASHBOARD).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.dashboard.DashboardDTO.get(getRequest(), getValue(DASHBOARD));
    }
  }
  
  public String getDashboardId()
  {
    return getValue(DASHBOARD);
  }
  
  public void setDashboard(net.geoprism.dashboard.DashboardDTO value)
  {
    if(value == null)
    {
      setValue(DASHBOARD, "");
    }
    else
    {
      setValue(DASHBOARD, value.getOid());
    }
  }
  
  public boolean isDashboardWritable()
  {
    return isWritable(DASHBOARD);
  }
  
  public boolean isDashboardReadable()
  {
    return isReadable(DASHBOARD);
  }
  
  public boolean isDashboardModified()
  {
    return isModified(DASHBOARD);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getDashboardMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(DASHBOARD).getAttributeMdDTO();
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
  
  public com.runwaysdk.system.metadata.MdClassDTO getWrappedMdClass()
  {
    if(getValue(WRAPPEDMDCLASS) == null || getValue(WRAPPEDMDCLASS).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdClassDTO.get(getRequest(), getValue(WRAPPEDMDCLASS));
    }
  }
  
  public String getWrappedMdClassId()
  {
    return getValue(WRAPPEDMDCLASS);
  }
  
  public void setWrappedMdClass(com.runwaysdk.system.metadata.MdClassDTO value)
  {
    if(value == null)
    {
      setValue(WRAPPEDMDCLASS, "");
    }
    else
    {
      setValue(WRAPPEDMDCLASS, value.getOid());
    }
  }
  
  public boolean isWrappedMdClassWritable()
  {
    return isWritable(WRAPPEDMDCLASS);
  }
  
  public boolean isWrappedMdClassReadable()
  {
    return isReadable(WRAPPEDMDCLASS);
  }
  
  public boolean isWrappedMdClassModified()
  {
    return isModified(WRAPPEDMDCLASS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getWrappedMdClassMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(WRAPPEDMDCLASS).getAttributeMdDTO();
  }
  
  public final net.geoprism.dashboard.MdAttributeViewDTO[] getSortedAttributes()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.MetadataWrapperDTO.CLASS, "getSortedAttributes", _declaredTypes);
    return (net.geoprism.dashboard.MdAttributeViewDTO[]) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final net.geoprism.dashboard.MdAttributeViewDTO[] getSortedAttributes(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.MetadataWrapperDTO.CLASS, "getSortedAttributes", _declaredTypes);
    return (net.geoprism.dashboard.MdAttributeViewDTO[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.dashboard.AttributeWrapperDTO> getAllAttributeWrapper()
  {
    return (java.util.List<? extends net.geoprism.dashboard.AttributeWrapperDTO>) getRequest().getChildren(this.getOid(), net.geoprism.dashboard.DashboardAttributesDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.dashboard.AttributeWrapperDTO> getAllAttributeWrapper(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.dashboard.AttributeWrapperDTO>) clientRequestIF.getChildren(oid, net.geoprism.dashboard.DashboardAttributesDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.dashboard.DashboardAttributesDTO> getAllAttributeWrapperRelationships()
  {
    return (java.util.List<? extends net.geoprism.dashboard.DashboardAttributesDTO>) getRequest().getChildRelationships(this.getOid(), net.geoprism.dashboard.DashboardAttributesDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.dashboard.DashboardAttributesDTO> getAllAttributeWrapperRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.dashboard.DashboardAttributesDTO>) clientRequestIF.getChildRelationships(oid, net.geoprism.dashboard.DashboardAttributesDTO.CLASS);
  }
  
  public net.geoprism.dashboard.DashboardAttributesDTO addAttributeWrapper(net.geoprism.dashboard.AttributeWrapperDTO child)
  {
    return (net.geoprism.dashboard.DashboardAttributesDTO) getRequest().addChild(this.getOid(), child.getOid(), net.geoprism.dashboard.DashboardAttributesDTO.CLASS);
  }
  
  public static net.geoprism.dashboard.DashboardAttributesDTO addAttributeWrapper(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, net.geoprism.dashboard.AttributeWrapperDTO child)
  {
    return (net.geoprism.dashboard.DashboardAttributesDTO) clientRequestIF.addChild(oid, child.getOid(), net.geoprism.dashboard.DashboardAttributesDTO.CLASS);
  }
  
  public void removeAttributeWrapper(net.geoprism.dashboard.DashboardAttributesDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeAttributeWrapper(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.dashboard.DashboardAttributesDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllAttributeWrapper()
  {
    getRequest().deleteChildren(this.getOid(), net.geoprism.dashboard.DashboardAttributesDTO.CLASS);
  }
  
  public static void removeAllAttributeWrapper(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, net.geoprism.dashboard.DashboardAttributesDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.dashboard.DashboardDTO> getAllDashboard()
  {
    return (java.util.List<? extends net.geoprism.dashboard.DashboardDTO>) getRequest().getParents(this.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.dashboard.DashboardDTO> getAllDashboard(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.dashboard.DashboardDTO>) clientRequestIF.getParents(oid, net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.dashboard.DashboardMetadataDTO> getAllDashboardRelationships()
  {
    return (java.util.List<? extends net.geoprism.dashboard.DashboardMetadataDTO>) getRequest().getParentRelationships(this.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.dashboard.DashboardMetadataDTO> getAllDashboardRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.dashboard.DashboardMetadataDTO>) clientRequestIF.getParentRelationships(oid, net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public net.geoprism.dashboard.DashboardMetadataDTO addDashboard(net.geoprism.dashboard.DashboardDTO parent)
  {
    return (net.geoprism.dashboard.DashboardMetadataDTO) getRequest().addParent(parent.getOid(), this.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public static net.geoprism.dashboard.DashboardMetadataDTO addDashboard(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, net.geoprism.dashboard.DashboardDTO parent)
  {
    return (net.geoprism.dashboard.DashboardMetadataDTO) clientRequestIF.addParent(parent.getOid(), oid, net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public void removeDashboard(net.geoprism.dashboard.DashboardMetadataDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeDashboard(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.dashboard.DashboardMetadataDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllDashboard()
  {
    getRequest().deleteParents(this.getOid(), net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public static void removeAllDashboard(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, net.geoprism.dashboard.DashboardMetadataDTO.CLASS);
  }
  
  public static net.geoprism.dashboard.MetadataWrapperDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.dashboard.MetadataWrapperDTO) dto;
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
  
  public static net.geoprism.dashboard.MetadataWrapperQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.dashboard.MetadataWrapperQueryDTO) clientRequest.getAllInstances(net.geoprism.dashboard.MetadataWrapperDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.dashboard.MetadataWrapperDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.MetadataWrapperDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.dashboard.MetadataWrapperDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.dashboard.MetadataWrapperDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.MetadataWrapperDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.dashboard.MetadataWrapperDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
