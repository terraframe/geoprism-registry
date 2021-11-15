package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -389156705)
public abstract class ListTypeVersionDTOBase extends com.runwaysdk.business.BusinessDTO
{
  public final static String CLASS = "net.geoprism.registry.ListTypeVersion";
  private static final long serialVersionUID = -389156705;
  
  protected ListTypeVersionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListTypeVersionDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String ENTRY = "entry";
  public static java.lang.String FORDATE = "forDate";
  public static java.lang.String GEOSPATIALDESCRIPTION = "geospatialDescription";
  public static java.lang.String GEOSPATIALMASTER = "geospatialMaster";
  public static java.lang.String GEOSPATIALVISIBILITY = "geospatialVisibility";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LISTDESCRIPTION = "listDescription";
  public static java.lang.String LISTMASTER = "listMaster";
  public static java.lang.String LISTTYPE = "listType";
  public static java.lang.String LISTVISIBILITY = "listVisibility";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String MDBUSINESS = "mdBusiness";
  public static java.lang.String OID = "oid";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String PUBLISHDATE = "publishDate";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TYPE = "type";
  public static java.lang.String VERSIONNUMBER = "versionNumber";
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
  
  public String getCreatedByOid()
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
  
  public String getEntityDomainOid()
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
  
  public net.geoprism.registry.ListTypeEntryDTO getEntry()
  {
    if(getValue(ENTRY) == null || getValue(ENTRY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.ListTypeEntryDTO.get(getRequest(), getValue(ENTRY));
    }
  }
  
  public String getEntryOid()
  {
    return getValue(ENTRY);
  }
  
  public void setEntry(net.geoprism.registry.ListTypeEntryDTO value)
  {
    if(value == null)
    {
      setValue(ENTRY, "");
    }
    else
    {
      setValue(ENTRY, value.getOid());
    }
  }
  
  public boolean isEntryWritable()
  {
    return isWritable(ENTRY);
  }
  
  public boolean isEntryReadable()
  {
    return isReadable(ENTRY);
  }
  
  public boolean isEntryModified()
  {
    return isModified(ENTRY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getEntryMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(ENTRY).getAttributeMdDTO();
  }
  
  public java.util.Date getForDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(FORDATE));
  }
  
  public void setForDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(FORDATE, "");
    }
    else
    {
      setValue(FORDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATETIME_FORMAT).format(value));
    }
  }
  
  public boolean isForDateWritable()
  {
    return isWritable(FORDATE);
  }
  
  public boolean isForDateReadable()
  {
    return isReadable(FORDATE);
  }
  
  public boolean isForDateModified()
  {
    return isModified(FORDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getForDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(FORDATE).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialDescriptionDTO getGeospatialDescription()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialDescriptionDTO) this.getAttributeStructDTO(GEOSPATIALDESCRIPTION).getStructDTO();
  }
  
  public boolean isGeospatialDescriptionWritable()
  {
    return isWritable(GEOSPATIALDESCRIPTION);
  }
  
  public boolean isGeospatialDescriptionReadable()
  {
    return isReadable(GEOSPATIALDESCRIPTION);
  }
  
  public boolean isGeospatialDescriptionModified()
  {
    return isModified(GEOSPATIALDESCRIPTION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialDescriptionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALDESCRIPTION).getAttributeMdDTO();
  }
  
  public Boolean getGeospatialMaster()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(GEOSPATIALMASTER));
  }
  
  public void setGeospatialMaster(Boolean value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALMASTER, "");
    }
    else
    {
      setValue(GEOSPATIALMASTER, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isGeospatialMasterWritable()
  {
    return isWritable(GEOSPATIALMASTER);
  }
  
  public boolean isGeospatialMasterReadable()
  {
    return isReadable(GEOSPATIALMASTER);
  }
  
  public boolean isGeospatialMasterModified()
  {
    return isModified(GEOSPATIALMASTER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getGeospatialMasterMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(GEOSPATIALMASTER).getAttributeMdDTO();
  }
  
  public String getGeospatialVisibility()
  {
    return getValue(GEOSPATIALVISIBILITY);
  }
  
  public void setGeospatialVisibility(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALVISIBILITY, "");
    }
    else
    {
      setValue(GEOSPATIALVISIBILITY, value);
    }
  }
  
  public boolean isGeospatialVisibilityWritable()
  {
    return isWritable(GEOSPATIALVISIBILITY);
  }
  
  public boolean isGeospatialVisibilityReadable()
  {
    return isReadable(GEOSPATIALVISIBILITY);
  }
  
  public boolean isGeospatialVisibilityModified()
  {
    return isModified(GEOSPATIALVISIBILITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGeospatialVisibilityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GEOSPATIALVISIBILITY).getAttributeMdDTO();
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
  
  public String getLastUpdatedByOid()
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
  
  public net.geoprism.registry.ListTypeVersionListDescriptionDTO getListDescription()
  {
    return (net.geoprism.registry.ListTypeVersionListDescriptionDTO) this.getAttributeStructDTO(LISTDESCRIPTION).getStructDTO();
  }
  
  public boolean isListDescriptionWritable()
  {
    return isWritable(LISTDESCRIPTION);
  }
  
  public boolean isListDescriptionReadable()
  {
    return isReadable(LISTDESCRIPTION);
  }
  
  public boolean isListDescriptionModified()
  {
    return isModified(LISTDESCRIPTION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListDescriptionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTDESCRIPTION).getAttributeMdDTO();
  }
  
  public Boolean getListMaster()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(LISTMASTER));
  }
  
  public void setListMaster(Boolean value)
  {
    if(value == null)
    {
      setValue(LISTMASTER, "");
    }
    else
    {
      setValue(LISTMASTER, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isListMasterWritable()
  {
    return isWritable(LISTMASTER);
  }
  
  public boolean isListMasterReadable()
  {
    return isReadable(LISTMASTER);
  }
  
  public boolean isListMasterModified()
  {
    return isModified(LISTMASTER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getListMasterMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(LISTMASTER).getAttributeMdDTO();
  }
  
  public boolean isListTypeWritable()
  {
    return isWritable(LISTTYPE);
  }
  
  public boolean isListTypeReadable()
  {
    return isReadable(LISTTYPE);
  }
  
  public boolean isListTypeModified()
  {
    return isModified(LISTTYPE);
  }
  
  public String getListVisibility()
  {
    return getValue(LISTVISIBILITY);
  }
  
  public void setListVisibility(String value)
  {
    if(value == null)
    {
      setValue(LISTVISIBILITY, "");
    }
    else
    {
      setValue(LISTVISIBILITY, value);
    }
  }
  
  public boolean isListVisibilityWritable()
  {
    return isWritable(LISTVISIBILITY);
  }
  
  public boolean isListVisibilityReadable()
  {
    return isReadable(LISTVISIBILITY);
  }
  
  public boolean isListVisibilityModified()
  {
    return isModified(LISTVISIBILITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getListVisibilityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LISTVISIBILITY).getAttributeMdDTO();
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
  
  public String getLockedByOid()
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
  
  public com.runwaysdk.system.metadata.MdBusinessDTO getMdBusiness()
  {
    if(getValue(MDBUSINESS) == null || getValue(MDBUSINESS).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdBusinessDTO.get(getRequest(), getValue(MDBUSINESS));
    }
  }
  
  public String getMdBusinessOid()
  {
    return getValue(MDBUSINESS);
  }
  
  public void setMdBusiness(com.runwaysdk.system.metadata.MdBusinessDTO value)
  {
    if(value == null)
    {
      setValue(MDBUSINESS, "");
    }
    else
    {
      setValue(MDBUSINESS, value.getOid());
    }
  }
  
  public boolean isMdBusinessWritable()
  {
    return isWritable(MDBUSINESS);
  }
  
  public boolean isMdBusinessReadable()
  {
    return isReadable(MDBUSINESS);
  }
  
  public boolean isMdBusinessModified()
  {
    return isModified(MDBUSINESS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getMdBusinessMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(MDBUSINESS).getAttributeMdDTO();
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
  
  public String getOwnerOid()
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
  
  public java.util.Date getPublishDate()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(PUBLISHDATE));
  }
  
  public void setPublishDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(PUBLISHDATE, "");
    }
    else
    {
      setValue(PUBLISHDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATE_FORMAT).format(value));
    }
  }
  
  public boolean isPublishDateWritable()
  {
    return isWritable(PUBLISHDATE);
  }
  
  public boolean isPublishDateReadable()
  {
    return isReadable(PUBLISHDATE);
  }
  
  public boolean isPublishDateModified()
  {
    return isModified(PUBLISHDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateMdDTO getPublishDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateMdDTO) getAttributeDTO(PUBLISHDATE).getAttributeMdDTO();
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
  
  public Integer getVersionNumber()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(VERSIONNUMBER));
  }
  
  public void setVersionNumber(Integer value)
  {
    if(value == null)
    {
      setValue(VERSIONNUMBER, "");
    }
    else
    {
      setValue(VERSIONNUMBER, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isVersionNumberWritable()
  {
    return isWritable(VERSIONNUMBER);
  }
  
  public boolean isVersionNumberReadable()
  {
    return isReadable(VERSIONNUMBER);
  }
  
  public boolean isVersionNumberModified()
  {
    return isModified(VERSIONNUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getVersionNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(VERSIONNUMBER).getAttributeMdDTO();
  }
  
  public final java.lang.String publish()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeVersionDTO.CLASS, "publish", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String publish(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeVersionDTO.CLASS, "publish", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static net.geoprism.registry.ListTypeVersionDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.ListTypeVersionDTO) dto;
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
  
  public static net.geoprism.registry.ListTypeVersionQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.ListTypeVersionQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.ListTypeVersionDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.ListTypeVersionDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeVersionDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.ListTypeVersionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.ListTypeVersionDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeVersionDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.ListTypeVersionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
