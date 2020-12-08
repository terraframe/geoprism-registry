package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -303964739)
public abstract class HierarchyMetadataDTOBase extends com.runwaysdk.business.BusinessDTO
{
  public final static String CLASS = "net.geoprism.registry.HierarchyMetadata";
  private static final long serialVersionUID = -303964739;
  
  protected HierarchyMetadataDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected HierarchyMetadataDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ABSTRACTDESCRIPTION = "abstractDescription";
  public static java.lang.String ACCESSCONSTRAINTS = "accessConstraints";
  public static java.lang.String ACKNOWLEDGEMENT = "acknowledgement";
  public static java.lang.String CONTACT = "contact";
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String DISCLAIMER = "disclaimer";
  public static java.lang.String EMAIL = "email";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String MDTERMRELATIONSHIP = "mdTermRelationship";
  public static java.lang.String OID = "oid";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String PHONENUMBER = "phoneNumber";
  public static java.lang.String PROGRESS = "progress";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TYPE = "type";
  public static java.lang.String USECONSTRAINTS = "useConstraints";
  public String getAbstractDescription()
  {
    return getValue(ABSTRACTDESCRIPTION);
  }
  
  public void setAbstractDescription(String value)
  {
    if(value == null)
    {
      setValue(ABSTRACTDESCRIPTION, "");
    }
    else
    {
      setValue(ABSTRACTDESCRIPTION, value);
    }
  }
  
  public boolean isAbstractDescriptionWritable()
  {
    return isWritable(ABSTRACTDESCRIPTION);
  }
  
  public boolean isAbstractDescriptionReadable()
  {
    return isReadable(ABSTRACTDESCRIPTION);
  }
  
  public boolean isAbstractDescriptionModified()
  {
    return isModified(ABSTRACTDESCRIPTION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getAbstractDescriptionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ABSTRACTDESCRIPTION).getAttributeMdDTO();
  }
  
  public String getAccessConstraints()
  {
    return getValue(ACCESSCONSTRAINTS);
  }
  
  public void setAccessConstraints(String value)
  {
    if(value == null)
    {
      setValue(ACCESSCONSTRAINTS, "");
    }
    else
    {
      setValue(ACCESSCONSTRAINTS, value);
    }
  }
  
  public boolean isAccessConstraintsWritable()
  {
    return isWritable(ACCESSCONSTRAINTS);
  }
  
  public boolean isAccessConstraintsReadable()
  {
    return isReadable(ACCESSCONSTRAINTS);
  }
  
  public boolean isAccessConstraintsModified()
  {
    return isModified(ACCESSCONSTRAINTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getAccessConstraintsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ACCESSCONSTRAINTS).getAttributeMdDTO();
  }
  
  public String getAcknowledgement()
  {
    return getValue(ACKNOWLEDGEMENT);
  }
  
  public void setAcknowledgement(String value)
  {
    if(value == null)
    {
      setValue(ACKNOWLEDGEMENT, "");
    }
    else
    {
      setValue(ACKNOWLEDGEMENT, value);
    }
  }
  
  public boolean isAcknowledgementWritable()
  {
    return isWritable(ACKNOWLEDGEMENT);
  }
  
  public boolean isAcknowledgementReadable()
  {
    return isReadable(ACKNOWLEDGEMENT);
  }
  
  public boolean isAcknowledgementModified()
  {
    return isModified(ACKNOWLEDGEMENT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getAcknowledgementMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ACKNOWLEDGEMENT).getAttributeMdDTO();
  }
  
  public String getContact()
  {
    return getValue(CONTACT);
  }
  
  public void setContact(String value)
  {
    if(value == null)
    {
      setValue(CONTACT, "");
    }
    else
    {
      setValue(CONTACT, value);
    }
  }
  
  public boolean isContactWritable()
  {
    return isWritable(CONTACT);
  }
  
  public boolean isContactReadable()
  {
    return isReadable(CONTACT);
  }
  
  public boolean isContactModified()
  {
    return isModified(CONTACT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getContactMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CONTACT).getAttributeMdDTO();
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
  
  public String getDisclaimer()
  {
    return getValue(DISCLAIMER);
  }
  
  public void setDisclaimer(String value)
  {
    if(value == null)
    {
      setValue(DISCLAIMER, "");
    }
    else
    {
      setValue(DISCLAIMER, value);
    }
  }
  
  public boolean isDisclaimerWritable()
  {
    return isWritable(DISCLAIMER);
  }
  
  public boolean isDisclaimerReadable()
  {
    return isReadable(DISCLAIMER);
  }
  
  public boolean isDisclaimerModified()
  {
    return isModified(DISCLAIMER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getDisclaimerMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(DISCLAIMER).getAttributeMdDTO();
  }
  
  public String getEmail()
  {
    return getValue(EMAIL);
  }
  
  public void setEmail(String value)
  {
    if(value == null)
    {
      setValue(EMAIL, "");
    }
    else
    {
      setValue(EMAIL, value);
    }
  }
  
  public boolean isEmailWritable()
  {
    return isWritable(EMAIL);
  }
  
  public boolean isEmailReadable()
  {
    return isReadable(EMAIL);
  }
  
  public boolean isEmailModified()
  {
    return isModified(EMAIL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getEmailMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(EMAIL).getAttributeMdDTO();
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
  
  public com.runwaysdk.system.metadata.MdTermRelationshipDTO getMdTermRelationship()
  {
    if(getValue(MDTERMRELATIONSHIP) == null || getValue(MDTERMRELATIONSHIP).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdTermRelationshipDTO.get(getRequest(), getValue(MDTERMRELATIONSHIP));
    }
  }
  
  public String getMdTermRelationshipOid()
  {
    return getValue(MDTERMRELATIONSHIP);
  }
  
  public void setMdTermRelationship(com.runwaysdk.system.metadata.MdTermRelationshipDTO value)
  {
    if(value == null)
    {
      setValue(MDTERMRELATIONSHIP, "");
    }
    else
    {
      setValue(MDTERMRELATIONSHIP, value.getOid());
    }
  }
  
  public boolean isMdTermRelationshipWritable()
  {
    return isWritable(MDTERMRELATIONSHIP);
  }
  
  public boolean isMdTermRelationshipReadable()
  {
    return isReadable(MDTERMRELATIONSHIP);
  }
  
  public boolean isMdTermRelationshipModified()
  {
    return isModified(MDTERMRELATIONSHIP);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getMdTermRelationshipMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(MDTERMRELATIONSHIP).getAttributeMdDTO();
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
  
  public String getPhoneNumber()
  {
    return getValue(PHONENUMBER);
  }
  
  public void setPhoneNumber(String value)
  {
    if(value == null)
    {
      setValue(PHONENUMBER, "");
    }
    else
    {
      setValue(PHONENUMBER, value);
    }
  }
  
  public boolean isPhoneNumberWritable()
  {
    return isWritable(PHONENUMBER);
  }
  
  public boolean isPhoneNumberReadable()
  {
    return isReadable(PHONENUMBER);
  }
  
  public boolean isPhoneNumberModified()
  {
    return isModified(PHONENUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getPhoneNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PHONENUMBER).getAttributeMdDTO();
  }
  
  public String getProgress()
  {
    return getValue(PROGRESS);
  }
  
  public void setProgress(String value)
  {
    if(value == null)
    {
      setValue(PROGRESS, "");
    }
    else
    {
      setValue(PROGRESS, value);
    }
  }
  
  public boolean isProgressWritable()
  {
    return isWritable(PROGRESS);
  }
  
  public boolean isProgressReadable()
  {
    return isReadable(PROGRESS);
  }
  
  public boolean isProgressModified()
  {
    return isModified(PROGRESS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getProgressMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PROGRESS).getAttributeMdDTO();
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
  
  public String getUseConstraints()
  {
    return getValue(USECONSTRAINTS);
  }
  
  public void setUseConstraints(String value)
  {
    if(value == null)
    {
      setValue(USECONSTRAINTS, "");
    }
    else
    {
      setValue(USECONSTRAINTS, value);
    }
  }
  
  public boolean isUseConstraintsWritable()
  {
    return isWritable(USECONSTRAINTS);
  }
  
  public boolean isUseConstraintsReadable()
  {
    return isReadable(USECONSTRAINTS);
  }
  
  public boolean isUseConstraintsModified()
  {
    return isModified(USECONSTRAINTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getUseConstraintsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(USECONSTRAINTS).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.HierarchyMetadataDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.HierarchyMetadataDTO) dto;
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
  
  public static net.geoprism.registry.HierarchyMetadataQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.HierarchyMetadataQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.HierarchyMetadataDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.HierarchyMetadataDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.HierarchyMetadataDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.HierarchyMetadataDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.HierarchyMetadataDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.HierarchyMetadataDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.HierarchyMetadataDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
