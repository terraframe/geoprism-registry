package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -923815224)
public abstract class MasterListDTOBase extends com.runwaysdk.business.BusinessDTO
{
  public final static String CLASS = "net.geoprism.registry.MasterList";
  private static final long serialVersionUID = -923815224;
  
  protected MasterListDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected MasterListDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ACCESSCONSTRAINTS = "accessConstraints";
  public static java.lang.String ACKNOWLEDGEMENTS = "acknowledgements";
  public static java.lang.String CODE = "code";
  public static java.lang.String CONTACTNAME = "contactName";
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String DISCLAIMER = "disclaimer";
  public static java.lang.String DISPLAYLABEL = "displayLabel";
  public static java.lang.String EMAIL = "email";
  public static java.lang.String ENTITYDOMAIN = "entityDomain";
  public static java.lang.String FREQUENCY = "frequency";
  public static java.lang.String HIERARCHIES = "hierarchies";
  public static java.lang.String ISMASTER = "isMaster";
  public static java.lang.String KEYNAME = "keyName";
  public static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static java.lang.String LISTABSTRACT = "listAbstract";
  public static java.lang.String LOCKEDBY = "lockedBy";
  public static java.lang.String OID = "oid";
  public static java.lang.String ORGANIZATION = "organization";
  public static java.lang.String OWNER = "owner";
  public static java.lang.String PROCESS = "process";
  public static java.lang.String PROGRESS = "progress";
  public static java.lang.String PUBLISHDATE = "publishDate";
  public static java.lang.String PUBLISHINGSTARTDATE = "publishingStartDate";
  public static java.lang.String REPRESENTATIVITYDATE = "representativityDate";
  public static java.lang.String SEQ = "seq";
  public static java.lang.String SITEMASTER = "siteMaster";
  public static java.lang.String TELEPHONENUMBER = "telephoneNumber";
  public static java.lang.String TYPE = "type";
  public static java.lang.String UNIVERSAL = "universal";
  public static java.lang.String USECONSTRAINTS = "useConstraints";
  public static java.lang.String VALID = "valid";
  public static java.lang.String VISIBILITY = "visibility";
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
  
  public String getAcknowledgements()
  {
    return getValue(ACKNOWLEDGEMENTS);
  }
  
  public void setAcknowledgements(String value)
  {
    if(value == null)
    {
      setValue(ACKNOWLEDGEMENTS, "");
    }
    else
    {
      setValue(ACKNOWLEDGEMENTS, value);
    }
  }
  
  public boolean isAcknowledgementsWritable()
  {
    return isWritable(ACKNOWLEDGEMENTS);
  }
  
  public boolean isAcknowledgementsReadable()
  {
    return isReadable(ACKNOWLEDGEMENTS);
  }
  
  public boolean isAcknowledgementsModified()
  {
    return isModified(ACKNOWLEDGEMENTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getAcknowledgementsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ACKNOWLEDGEMENTS).getAttributeMdDTO();
  }
  
  public String getCode()
  {
    return getValue(CODE);
  }
  
  public void setCode(String value)
  {
    if(value == null)
    {
      setValue(CODE, "");
    }
    else
    {
      setValue(CODE, value);
    }
  }
  
  public boolean isCodeWritable()
  {
    return isWritable(CODE);
  }
  
  public boolean isCodeReadable()
  {
    return isReadable(CODE);
  }
  
  public boolean isCodeModified()
  {
    return isModified(CODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CODE).getAttributeMdDTO();
  }
  
  public String getContactName()
  {
    return getValue(CONTACTNAME);
  }
  
  public void setContactName(String value)
  {
    if(value == null)
    {
      setValue(CONTACTNAME, "");
    }
    else
    {
      setValue(CONTACTNAME, value);
    }
  }
  
  public boolean isContactNameWritable()
  {
    return isWritable(CONTACTNAME);
  }
  
  public boolean isContactNameReadable()
  {
    return isReadable(CONTACTNAME);
  }
  
  public boolean isContactNameModified()
  {
    return isModified(CONTACTNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getContactNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CONTACTNAME).getAttributeMdDTO();
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
  
  public net.geoprism.registry.MasterListDisplayLabelDTO getDisplayLabel()
  {
    return (net.geoprism.registry.MasterListDisplayLabelDTO) this.getAttributeStructDTO(DISPLAYLABEL).getStructDTO();
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
  
  @SuppressWarnings("unchecked")
  public java.util.List<net.geoprism.registry.ChangeFrequencyDTO> getFrequency()
  {
    return (java.util.List<net.geoprism.registry.ChangeFrequencyDTO>) com.runwaysdk.transport.conversion.ConversionFacade.convertEnumDTOsFromEnumNames(getRequest(), net.geoprism.registry.ChangeFrequencyDTO.CLASS, getEnumNames(FREQUENCY));
  }
  
  public java.util.List<String> getFrequencyEnumNames()
  {
    return getEnumNames(FREQUENCY);
  }
  
  public void addFrequency(net.geoprism.registry.ChangeFrequencyDTO enumDTO)
  {
    addEnumItem(FREQUENCY, enumDTO.toString());
  }
  
  public void removeFrequency(net.geoprism.registry.ChangeFrequencyDTO enumDTO)
  {
    removeEnumItem(FREQUENCY, enumDTO.toString());
  }
  
  public void clearFrequency()
  {
    clearEnum(FREQUENCY);
  }
  
  public boolean isFrequencyWritable()
  {
    return isWritable(FREQUENCY);
  }
  
  public boolean isFrequencyReadable()
  {
    return isReadable(FREQUENCY);
  }
  
  public boolean isFrequencyModified()
  {
    return isModified(FREQUENCY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO getFrequencyMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO) getAttributeDTO(FREQUENCY).getAttributeMdDTO();
  }
  
  public String getHierarchies()
  {
    return getValue(HIERARCHIES);
  }
  
  public void setHierarchies(String value)
  {
    if(value == null)
    {
      setValue(HIERARCHIES, "");
    }
    else
    {
      setValue(HIERARCHIES, value);
    }
  }
  
  public boolean isHierarchiesWritable()
  {
    return isWritable(HIERARCHIES);
  }
  
  public boolean isHierarchiesReadable()
  {
    return isReadable(HIERARCHIES);
  }
  
  public boolean isHierarchiesModified()
  {
    return isModified(HIERARCHIES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getHierarchiesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(HIERARCHIES).getAttributeMdDTO();
  }
  
  public Boolean getIsMaster()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(ISMASTER));
  }
  
  public void setIsMaster(Boolean value)
  {
    if(value == null)
    {
      setValue(ISMASTER, "");
    }
    else
    {
      setValue(ISMASTER, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isIsMasterWritable()
  {
    return isWritable(ISMASTER);
  }
  
  public boolean isIsMasterReadable()
  {
    return isReadable(ISMASTER);
  }
  
  public boolean isIsMasterModified()
  {
    return isModified(ISMASTER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getIsMasterMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(ISMASTER).getAttributeMdDTO();
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
  
  public String getListAbstract()
  {
    return getValue(LISTABSTRACT);
  }
  
  public void setListAbstract(String value)
  {
    if(value == null)
    {
      setValue(LISTABSTRACT, "");
    }
    else
    {
      setValue(LISTABSTRACT, value);
    }
  }
  
  public boolean isListAbstractWritable()
  {
    return isWritable(LISTABSTRACT);
  }
  
  public boolean isListAbstractReadable()
  {
    return isReadable(LISTABSTRACT);
  }
  
  public boolean isListAbstractModified()
  {
    return isModified(LISTABSTRACT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getListAbstractMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(LISTABSTRACT).getAttributeMdDTO();
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
  
  public boolean isOrganizationWritable()
  {
    return isWritable(ORGANIZATION);
  }
  
  public boolean isOrganizationReadable()
  {
    return isReadable(ORGANIZATION);
  }
  
  public boolean isOrganizationModified()
  {
    return isModified(ORGANIZATION);
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
  
  public String getProcess()
  {
    return getValue(PROCESS);
  }
  
  public void setProcess(String value)
  {
    if(value == null)
    {
      setValue(PROCESS, "");
    }
    else
    {
      setValue(PROCESS, value);
    }
  }
  
  public boolean isProcessWritable()
  {
    return isWritable(PROCESS);
  }
  
  public boolean isProcessReadable()
  {
    return isReadable(PROCESS);
  }
  
  public boolean isProcessModified()
  {
    return isModified(PROCESS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getProcessMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PROCESS).getAttributeMdDTO();
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
  
  public java.util.Date getPublishingStartDate()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(PUBLISHINGSTARTDATE));
  }
  
  public void setPublishingStartDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(PUBLISHINGSTARTDATE, "");
    }
    else
    {
      setValue(PUBLISHINGSTARTDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATE_FORMAT).format(value));
    }
  }
  
  public boolean isPublishingStartDateWritable()
  {
    return isWritable(PUBLISHINGSTARTDATE);
  }
  
  public boolean isPublishingStartDateReadable()
  {
    return isReadable(PUBLISHINGSTARTDATE);
  }
  
  public boolean isPublishingStartDateModified()
  {
    return isModified(PUBLISHINGSTARTDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateMdDTO getPublishingStartDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateMdDTO) getAttributeDTO(PUBLISHINGSTARTDATE).getAttributeMdDTO();
  }
  
  public java.util.Date getRepresentativityDate()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(REPRESENTATIVITYDATE));
  }
  
  public void setRepresentativityDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(REPRESENTATIVITYDATE, "");
    }
    else
    {
      setValue(REPRESENTATIVITYDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATE_FORMAT).format(value));
    }
  }
  
  public boolean isRepresentativityDateWritable()
  {
    return isWritable(REPRESENTATIVITYDATE);
  }
  
  public boolean isRepresentativityDateReadable()
  {
    return isReadable(REPRESENTATIVITYDATE);
  }
  
  public boolean isRepresentativityDateModified()
  {
    return isModified(REPRESENTATIVITYDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateMdDTO getRepresentativityDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateMdDTO) getAttributeDTO(REPRESENTATIVITYDATE).getAttributeMdDTO();
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
  
  public String getTelephoneNumber()
  {
    return getValue(TELEPHONENUMBER);
  }
  
  public void setTelephoneNumber(String value)
  {
    if(value == null)
    {
      setValue(TELEPHONENUMBER, "");
    }
    else
    {
      setValue(TELEPHONENUMBER, value);
    }
  }
  
  public boolean isTelephoneNumberWritable()
  {
    return isWritable(TELEPHONENUMBER);
  }
  
  public boolean isTelephoneNumberReadable()
  {
    return isReadable(TELEPHONENUMBER);
  }
  
  public boolean isTelephoneNumberModified()
  {
    return isModified(TELEPHONENUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getTelephoneNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(TELEPHONENUMBER).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.gis.geo.UniversalDTO getUniversal()
  {
    if(getValue(UNIVERSAL) == null || getValue(UNIVERSAL).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.gis.geo.UniversalDTO.get(getRequest(), getValue(UNIVERSAL));
    }
  }
  
  public String getUniversalOid()
  {
    return getValue(UNIVERSAL);
  }
  
  public void setUniversal(com.runwaysdk.system.gis.geo.UniversalDTO value)
  {
    if(value == null)
    {
      setValue(UNIVERSAL, "");
    }
    else
    {
      setValue(UNIVERSAL, value.getOid());
    }
  }
  
  public boolean isUniversalWritable()
  {
    return isWritable(UNIVERSAL);
  }
  
  public boolean isUniversalReadable()
  {
    return isReadable(UNIVERSAL);
  }
  
  public boolean isUniversalModified()
  {
    return isModified(UNIVERSAL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getUniversalMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(UNIVERSAL).getAttributeMdDTO();
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
  
  public Boolean getValid()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(VALID));
  }
  
  public void setValid(Boolean value)
  {
    if(value == null)
    {
      setValue(VALID, "");
    }
    else
    {
      setValue(VALID, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isValidWritable()
  {
    return isWritable(VALID);
  }
  
  public boolean isValidReadable()
  {
    return isReadable(VALID);
  }
  
  public boolean isValidModified()
  {
    return isModified(VALID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getValidMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(VALID).getAttributeMdDTO();
  }
  
  public String getVisibility()
  {
    return getValue(VISIBILITY);
  }
  
  public void setVisibility(String value)
  {
    if(value == null)
    {
      setValue(VISIBILITY, "");
    }
    else
    {
      setValue(VISIBILITY, value);
    }
  }
  
  public boolean isVisibilityWritable()
  {
    return isWritable(VISIBILITY);
  }
  
  public boolean isVisibilityReadable()
  {
    return isReadable(VISIBILITY);
  }
  
  public boolean isVisibilityModified()
  {
    return isModified(VISIBILITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getVisibilityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(VISIBILITY).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.MasterListDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.MasterListDTO) dto;
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
  
  public static net.geoprism.registry.MasterListQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.MasterListQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.MasterListDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.MasterListDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.MasterListDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.MasterListDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.MasterListDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.MasterListDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.MasterListDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
