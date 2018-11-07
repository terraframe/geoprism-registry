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
package net.geoprism.ontology;

@com.runwaysdk.business.ClassSignature(hash = -2015924917)
public abstract class ClassifierDTOBase extends com.runwaysdk.business.ontology.TermDTO 
{
  public final static String CLASS = "net.geoprism.ontology.Classifier";
  private static final long serialVersionUID = -2015924917;
  
  protected ClassifierDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ClassifierDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CATEGORY = "category";
  public static java.lang.String CLASSIFIERID = "classifierId";
  public static java.lang.String CLASSIFIERPACKAGE = "classifierPackage";
  public static java.lang.String CREATEDATE = "createDate";
  public static java.lang.String CREATEDBY = "createdBy";
  public static java.lang.String DISPLAYLABEL = "displayLabel";
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
  public Boolean getCategory()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(CATEGORY));
  }
  
  public void setCategory(Boolean value)
  {
    if(value == null)
    {
      setValue(CATEGORY, "");
    }
    else
    {
      setValue(CATEGORY, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isCategoryWritable()
  {
    return isWritable(CATEGORY);
  }
  
  public boolean isCategoryReadable()
  {
    return isReadable(CATEGORY);
  }
  
  public boolean isCategoryModified()
  {
    return isModified(CATEGORY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getCategoryMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(CATEGORY).getAttributeMdDTO();
  }
  
  public String getClassifierId()
  {
    return getValue(CLASSIFIERID);
  }
  
  public void setClassifierId(String value)
  {
    if(value == null)
    {
      setValue(CLASSIFIERID, "");
    }
    else
    {
      setValue(CLASSIFIERID, value);
    }
  }
  
  public boolean isClassifierIdWritable()
  {
    return isWritable(CLASSIFIERID);
  }
  
  public boolean isClassifierIdReadable()
  {
    return isReadable(CLASSIFIERID);
  }
  
  public boolean isClassifierIdModified()
  {
    return isModified(CLASSIFIERID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getClassifierIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CLASSIFIERID).getAttributeMdDTO();
  }
  
  public String getClassifierPackage()
  {
    return getValue(CLASSIFIERPACKAGE);
  }
  
  public void setClassifierPackage(String value)
  {
    if(value == null)
    {
      setValue(CLASSIFIERPACKAGE, "");
    }
    else
    {
      setValue(CLASSIFIERPACKAGE, value);
    }
  }
  
  public boolean isClassifierPackageWritable()
  {
    return isWritable(CLASSIFIERPACKAGE);
  }
  
  public boolean isClassifierPackageReadable()
  {
    return isReadable(CLASSIFIERPACKAGE);
  }
  
  public boolean isClassifierPackageModified()
  {
    return isModified(CLASSIFIERPACKAGE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getClassifierPackageMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CLASSIFIERPACKAGE).getAttributeMdDTO();
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
  
  public net.geoprism.ontology.ClassifierDisplayLabelDTO getDisplayLabel()
  {
    return (net.geoprism.ontology.ClassifierDisplayLabelDTO) this.getAttributeStructDTO(DISPLAYLABEL).getStructDTO();
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
  
  public static final void applyOption(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String config)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{config};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "applyOption", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final com.runwaysdk.business.ontology.TermAndRelDTO create(com.runwaysdk.constants.ClientRequestIF clientRequest, net.geoprism.ontology.ClassifierDTO dto, java.lang.String parentOid)
  {
    String[] _declaredTypes = new String[]{"net.geoprism.ontology.Classifier", "java.lang.String"};
    Object[] _parameters = new Object[]{dto, parentOid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "create", _declaredTypes);
    return (com.runwaysdk.business.ontology.TermAndRelDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.ontology.ClassifierDTO createOption(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String option)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{option};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "createOption", _declaredTypes);
    return (net.geoprism.ontology.ClassifierDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void deleteClassifierProblem(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String problemId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{problemId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "deleteClassifierProblem", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void deleteOption(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "deleteOption", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.ontology.ClassifierDTO editOption(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "editOption", _declaredTypes);
    return (net.geoprism.ontology.ClassifierDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.ontology.ClassifierProblemViewDTO[] getAllProblems(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "getAllProblems", _declaredTypes);
    return (net.geoprism.ontology.ClassifierProblemViewDTO[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String getCategoryClassifiersAsJSON(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "getCategoryClassifiersAsJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final com.runwaysdk.business.ValueQueryDTO getClassifierSuggestions(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String mdAttributeId, java.lang.String text, java.lang.Integer limit)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.Integer"};
    Object[] _parameters = new Object[]{mdAttributeId, text, limit};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "getClassifierSuggestions", _declaredTypes);
    return (com.runwaysdk.business.ValueQueryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String getClassifierTree(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String classifierId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{classifierId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "getClassifierTree", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.ontology.ClassifierDTO getRoot(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "getRoot", _declaredTypes);
    return (net.geoprism.ontology.ClassifierDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String[] makeSynonym(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String sourceId, java.lang.String destinationId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{sourceId, destinationId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "makeSynonym", _declaredTypes);
    return (java.lang.String[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String[] restoreSynonym(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String synonymId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{synonymId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "restoreSynonym", _declaredTypes);
    return (java.lang.String[]) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void unlockCategory(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "unlockCategory", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void validateCategoryName(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String name, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{name, oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "validateCategoryName", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierSynonymDTO> getAllHasSynonym()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierSynonymDTO>) getRequest().getChildren(this.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierSynonymDTO> getAllHasSynonym(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierSynonymDTO>) clientRequestIF.getChildren(oid, net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierHasSynonymDTO> getAllHasSynonymRelationships()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierHasSynonymDTO>) getRequest().getChildRelationships(this.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierHasSynonymDTO> getAllHasSynonymRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierHasSynonymDTO>) clientRequestIF.getChildRelationships(oid, net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public net.geoprism.ontology.ClassifierHasSynonymDTO addHasSynonym(net.geoprism.ontology.ClassifierSynonymDTO child)
  {
    return (net.geoprism.ontology.ClassifierHasSynonymDTO) getRequest().addChild(this.getOid(), child.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierHasSynonymDTO addHasSynonym(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, net.geoprism.ontology.ClassifierSynonymDTO child)
  {
    return (net.geoprism.ontology.ClassifierHasSynonymDTO) clientRequestIF.addChild(oid, child.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public void removeHasSynonym(net.geoprism.ontology.ClassifierHasSynonymDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeHasSynonym(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.ontology.ClassifierHasSynonymDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllHasSynonym()
  {
    getRequest().deleteChildren(this.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public static void removeAllHasSynonym(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierDTO> getAllIsAChild()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierDTO>) getRequest().getChildren(this.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierDTO> getAllIsAChild(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierDTO>) clientRequestIF.getChildren(oid, net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierIsARelationshipDTO> getAllIsAChildRelationships()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierIsARelationshipDTO>) getRequest().getChildRelationships(this.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierIsARelationshipDTO> getAllIsAChildRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierIsARelationshipDTO>) clientRequestIF.getChildRelationships(oid, net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public net.geoprism.ontology.ClassifierIsARelationshipDTO addIsAChild(net.geoprism.ontology.ClassifierDTO child)
  {
    return (net.geoprism.ontology.ClassifierIsARelationshipDTO) getRequest().addChild(this.getOid(), child.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierIsARelationshipDTO addIsAChild(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, net.geoprism.ontology.ClassifierDTO child)
  {
    return (net.geoprism.ontology.ClassifierIsARelationshipDTO) clientRequestIF.addChild(oid, child.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public void removeIsAChild(net.geoprism.ontology.ClassifierIsARelationshipDTO relationship)
  {
    getRequest().deleteChild(relationship.getOid());
  }
  
  public static void removeIsAChild(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.ontology.ClassifierIsARelationshipDTO relationship)
  {
    clientRequestIF.deleteChild(relationship.getOid());
  }
  
  public void removeAllIsAChild()
  {
    getRequest().deleteChildren(this.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public static void removeAllIsAChild(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteChildren(oid, net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeMultiTermDTO> getAllClassifierMultiTermAttributeRoots()
  {
    return (java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeMultiTermDTO>) getRequest().getParents(this.getOid(), net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeMultiTermDTO> getAllClassifierMultiTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeMultiTermDTO>) clientRequestIF.getParents(oid, net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO> getAllClassifierMultiTermAttributeRootsRelationships()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO>) getRequest().getParentRelationships(this.getOid(), net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO> getAllClassifierMultiTermAttributeRootsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO>) clientRequestIF.getParentRelationships(oid, net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO.CLASS);
  }
  
  public net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO addClassifierMultiTermAttributeRoots(com.runwaysdk.system.metadata.MdAttributeMultiTermDTO parent)
  {
    return (net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO) getRequest().addParent(parent.getOid(), this.getOid(), net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO addClassifierMultiTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, com.runwaysdk.system.metadata.MdAttributeMultiTermDTO parent)
  {
    return (net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO) clientRequestIF.addParent(parent.getOid(), oid, net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO.CLASS);
  }
  
  public void removeClassifierMultiTermAttributeRoots(net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeClassifierMultiTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllClassifierMultiTermAttributeRoots()
  {
    getRequest().deleteParents(this.getOid(), net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO.CLASS);
  }
  
  public static void removeAllClassifierMultiTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, net.geoprism.ontology.ClassifierMultiTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeTermDTO> getAllClassifierTermAttributeRoots()
  {
    return (java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeTermDTO>) getRequest().getParents(this.getOid(), net.geoprism.ontology.ClassifierTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeTermDTO> getAllClassifierTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeTermDTO>) clientRequestIF.getParents(oid, net.geoprism.ontology.ClassifierTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierTermAttributeRootDTO> getAllClassifierTermAttributeRootsRelationships()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierTermAttributeRootDTO>) getRequest().getParentRelationships(this.getOid(), net.geoprism.ontology.ClassifierTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierTermAttributeRootDTO> getAllClassifierTermAttributeRootsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierTermAttributeRootDTO>) clientRequestIF.getParentRelationships(oid, net.geoprism.ontology.ClassifierTermAttributeRootDTO.CLASS);
  }
  
  public net.geoprism.ontology.ClassifierTermAttributeRootDTO addClassifierTermAttributeRoots(com.runwaysdk.system.metadata.MdAttributeTermDTO parent)
  {
    return (net.geoprism.ontology.ClassifierTermAttributeRootDTO) getRequest().addParent(parent.getOid(), this.getOid(), net.geoprism.ontology.ClassifierTermAttributeRootDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierTermAttributeRootDTO addClassifierTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, com.runwaysdk.system.metadata.MdAttributeTermDTO parent)
  {
    return (net.geoprism.ontology.ClassifierTermAttributeRootDTO) clientRequestIF.addParent(parent.getOid(), oid, net.geoprism.ontology.ClassifierTermAttributeRootDTO.CLASS);
  }
  
  public void removeClassifierTermAttributeRoots(net.geoprism.ontology.ClassifierTermAttributeRootDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeClassifierTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.ontology.ClassifierTermAttributeRootDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllClassifierTermAttributeRoots()
  {
    getRequest().deleteParents(this.getOid(), net.geoprism.ontology.ClassifierTermAttributeRootDTO.CLASS);
  }
  
  public static void removeAllClassifierTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, net.geoprism.ontology.ClassifierTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierDTO> getAllIsAParent()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierDTO>) getRequest().getParents(this.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierDTO> getAllIsAParent(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierDTO>) clientRequestIF.getParents(oid, net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierIsARelationshipDTO> getAllIsAParentRelationships()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierIsARelationshipDTO>) getRequest().getParentRelationships(this.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierIsARelationshipDTO> getAllIsAParentRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierIsARelationshipDTO>) clientRequestIF.getParentRelationships(oid, net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public net.geoprism.ontology.ClassifierIsARelationshipDTO addIsAParent(net.geoprism.ontology.ClassifierDTO parent)
  {
    return (net.geoprism.ontology.ClassifierIsARelationshipDTO) getRequest().addParent(parent.getOid(), this.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierIsARelationshipDTO addIsAParent(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, net.geoprism.ontology.ClassifierDTO parent)
  {
    return (net.geoprism.ontology.ClassifierIsARelationshipDTO) clientRequestIF.addParent(parent.getOid(), oid, net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public void removeIsAParent(net.geoprism.ontology.ClassifierIsARelationshipDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeIsAParent(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.ontology.ClassifierIsARelationshipDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllIsAParent()
  {
    getRequest().deleteParents(this.getOid(), net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public static void removeAllIsAParent(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, net.geoprism.ontology.ClassifierIsARelationshipDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.ontology.ClassifierDTO) dto;
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
  
  public static net.geoprism.ontology.ClassifierQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.ontology.ClassifierQueryDTO) clientRequest.getAllInstances(net.geoprism.ontology.ClassifierDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.ontology.ClassifierDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.ontology.ClassifierDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.ontology.ClassifierDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.ontology.ClassifierDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
