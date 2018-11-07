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

@com.runwaysdk.business.ClassSignature(hash = 1549308703)
public abstract class ClassifierSynonymDTOBase extends com.runwaysdk.business.ontology.TermDTO 
{
  public final static String CLASS = "net.geoprism.ontology.ClassifierSynonym";
  private static final long serialVersionUID = 1549308703;
  
  protected ClassifierSynonymDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ClassifierSynonymDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CLASSIFIER = "classifier";
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
  public net.geoprism.ontology.ClassifierDTO getClassifier()
  {
    if(getValue(CLASSIFIER) == null || getValue(CLASSIFIER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.ontology.ClassifierDTO.get(getRequest(), getValue(CLASSIFIER));
    }
  }
  
  public String getClassifierId()
  {
    return getValue(CLASSIFIER);
  }
  
  public void setClassifier(net.geoprism.ontology.ClassifierDTO value)
  {
    if(value == null)
    {
      setValue(CLASSIFIER, "");
    }
    else
    {
      setValue(CLASSIFIER, value.getOid());
    }
  }
  
  public boolean isClassifierWritable()
  {
    return isWritable(CLASSIFIER);
  }
  
  public boolean isClassifierReadable()
  {
    return isReadable(CLASSIFIER);
  }
  
  public boolean isClassifierModified()
  {
    return isModified(CLASSIFIER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getClassifierMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(CLASSIFIER).getAttributeMdDTO();
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
  
  public net.geoprism.ontology.ClassifierSynonymDisplayLabelDTO getDisplayLabel()
  {
    return (net.geoprism.ontology.ClassifierSynonymDisplayLabelDTO) this.getAttributeStructDTO(DISPLAYLABEL).getStructDTO();
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
  
  public static final net.geoprism.ontology.ClassifierSynonymDTO create(com.runwaysdk.constants.ClientRequestIF clientRequest, net.geoprism.ontology.ClassifierDTO classifier, net.geoprism.ontology.ClassifierSynonymDTO synonym)
  {
    String[] _declaredTypes = new String[]{"net.geoprism.ontology.Classifier", "net.geoprism.ontology.ClassifierSynonym"};
    Object[] _parameters = new Object[]{classifier, synonym};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierSynonymDTO.CLASS, "create", _declaredTypes);
    return (net.geoprism.ontology.ClassifierSynonymDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final com.runwaysdk.business.ontology.TermAndRelDTO createSynonym(com.runwaysdk.constants.ClientRequestIF clientRequest, net.geoprism.ontology.ClassifierSynonymDTO synonym, java.lang.String classifierId)
  {
    String[] _declaredTypes = new String[]{"net.geoprism.ontology.ClassifierSynonym", "java.lang.String"};
    Object[] _parameters = new Object[]{synonym, classifierId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierSynonymDTO.CLASS, "createSynonym", _declaredTypes);
    return (com.runwaysdk.business.ontology.TermAndRelDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeMultiTermDTO> getAllClassifierSynonymMultiTermAttributeRoots()
  {
    return (java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeMultiTermDTO>) getRequest().getParents(this.getOid(), net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeMultiTermDTO> getAllClassifierSynonymMultiTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeMultiTermDTO>) clientRequestIF.getParents(oid, net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO> getAllClassifierSynonymMultiTermAttributeRootsRelationships()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO>) getRequest().getParentRelationships(this.getOid(), net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO> getAllClassifierSynonymMultiTermAttributeRootsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO>) clientRequestIF.getParentRelationships(oid, net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO.CLASS);
  }
  
  public net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO addClassifierSynonymMultiTermAttributeRoots(com.runwaysdk.system.metadata.MdAttributeMultiTermDTO parent)
  {
    return (net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO) getRequest().addParent(parent.getOid(), this.getOid(), net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO addClassifierSynonymMultiTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, com.runwaysdk.system.metadata.MdAttributeMultiTermDTO parent)
  {
    return (net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO) clientRequestIF.addParent(parent.getOid(), oid, net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO.CLASS);
  }
  
  public void removeClassifierSynonymMultiTermAttributeRoots(net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeClassifierSynonymMultiTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllClassifierSynonymMultiTermAttributeRoots()
  {
    getRequest().deleteParents(this.getOid(), net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO.CLASS);
  }
  
  public static void removeAllClassifierSynonymMultiTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, net.geoprism.ontology.ClassifierSynonymMultiTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeTermDTO> getAllClassifierSynonymTermAttributeRoots()
  {
    return (java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeTermDTO>) getRequest().getParents(this.getOid(), net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeTermDTO> getAllClassifierSynonymTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends com.runwaysdk.system.metadata.MdAttributeTermDTO>) clientRequestIF.getParents(oid, net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO> getAllClassifierSynonymTermAttributeRootsRelationships()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO>) getRequest().getParentRelationships(this.getOid(), net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO> getAllClassifierSynonymTermAttributeRootsRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO>) clientRequestIF.getParentRelationships(oid, net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO.CLASS);
  }
  
  public net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO addClassifierSynonymTermAttributeRoots(com.runwaysdk.system.metadata.MdAttributeTermDTO parent)
  {
    return (net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO) getRequest().addParent(parent.getOid(), this.getOid(), net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO addClassifierSynonymTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, com.runwaysdk.system.metadata.MdAttributeTermDTO parent)
  {
    return (net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO) clientRequestIF.addParent(parent.getOid(), oid, net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO.CLASS);
  }
  
  public void removeClassifierSynonymTermAttributeRoots(net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeClassifierSynonymTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllClassifierSynonymTermAttributeRoots()
  {
    getRequest().deleteParents(this.getOid(), net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO.CLASS);
  }
  
  public static void removeAllClassifierSynonymTermAttributeRoots(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, net.geoprism.ontology.ClassifierSynonymTermAttributeRootDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierDTO> getAllIsSynonymFor()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierDTO>) getRequest().getParents(this.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierDTO> getAllIsSynonymFor(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierDTO>) clientRequestIF.getParents(oid, net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<? extends net.geoprism.ontology.ClassifierHasSynonymDTO> getAllIsSynonymForRelationships()
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierHasSynonymDTO>) getRequest().getParentRelationships(this.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public static java.util.List<? extends net.geoprism.ontology.ClassifierHasSynonymDTO> getAllIsSynonymForRelationships(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    return (java.util.List<? extends net.geoprism.ontology.ClassifierHasSynonymDTO>) clientRequestIF.getParentRelationships(oid, net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public net.geoprism.ontology.ClassifierHasSynonymDTO addIsSynonymFor(net.geoprism.ontology.ClassifierDTO parent)
  {
    return (net.geoprism.ontology.ClassifierHasSynonymDTO) getRequest().addParent(parent.getOid(), this.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierHasSynonymDTO addIsSynonymFor(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid, net.geoprism.ontology.ClassifierDTO parent)
  {
    return (net.geoprism.ontology.ClassifierHasSynonymDTO) clientRequestIF.addParent(parent.getOid(), oid, net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public void removeIsSynonymFor(net.geoprism.ontology.ClassifierHasSynonymDTO relationship)
  {
    getRequest().deleteParent(relationship.getOid());
  }
  
  public static void removeIsSynonymFor(com.runwaysdk.constants.ClientRequestIF clientRequestIF, net.geoprism.ontology.ClassifierHasSynonymDTO relationship)
  {
    clientRequestIF.deleteParent(relationship.getOid());
  }
  
  public void removeAllIsSynonymFor()
  {
    getRequest().deleteParents(this.getOid(), net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public static void removeAllIsSynonymFor(com.runwaysdk.constants.ClientRequestIF clientRequestIF, String oid)
  {
    clientRequestIF.deleteParents(oid, net.geoprism.ontology.ClassifierHasSynonymDTO.CLASS);
  }
  
  public static net.geoprism.ontology.ClassifierSynonymDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.ontology.ClassifierSynonymDTO) dto;
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
  
  public static net.geoprism.ontology.ClassifierSynonymQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.ontology.ClassifierSynonymQueryDTO) clientRequest.getAllInstances(net.geoprism.ontology.ClassifierSynonymDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.ontology.ClassifierSynonymDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierSynonymDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.ontology.ClassifierSynonymDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.ontology.ClassifierSynonymDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.ontology.ClassifierSynonymDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.ontology.ClassifierSynonymDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
