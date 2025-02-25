/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -1037763527)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to SynchronizationConfig.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class SynchronizationConfigBase extends com.runwaysdk.business.Business
{
  public final static String CLASS = "net.geoprism.registry.SynchronizationConfig";
  public final static java.lang.String CONFIGURATION = "configuration";
  public final static java.lang.String CREATEDATE = "createDate";
  public final static java.lang.String CREATEDBY = "createdBy";
  public final static java.lang.String ENTITYDOMAIN = "entityDomain";
  public final static java.lang.String GRAPHHIERARCHY = "graphHierarchy";
  public final static java.lang.String ISIMPORT = "isImport";
  public final static java.lang.String KEYNAME = "keyName";
  public final static java.lang.String LABEL = "label";
  private com.runwaysdk.business.Struct label = null;
  
  public final static java.lang.String LASTSYNCHDATE = "lastSynchDate";
  public final static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public final static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public final static java.lang.String LOCKEDBY = "lockedBy";
  public final static java.lang.String OID = "oid";
  public final static java.lang.String ORGANIZATION = "organization";
  public final static java.lang.String OWNER = "owner";
  public final static java.lang.String SEQ = "seq";
  public final static java.lang.String SITEMASTER = "siteMaster";
  public final static java.lang.String SYSTEM = "system";
  public final static java.lang.String TYPE = "type";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1037763527;
  
  public SynchronizationConfigBase()
  {
    super();
    label = super.getStruct("label");
  }
  
  public String getConfiguration()
  {
    return getValue(CONFIGURATION);
  }
  
  public void validateConfiguration()
  {
    this.validateAttribute(CONFIGURATION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getConfigurationMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(CONFIGURATION);
  }
  
  public void setConfiguration(String value)
  {
    if(value == null)
    {
      setValue(CONFIGURATION, "");
    }
    else
    {
      setValue(CONFIGURATION, value);
    }
  }
  
  public java.util.Date getCreateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(CREATEDATE));
  }
  
  public void validateCreateDate()
  {
    this.validateAttribute(CREATEDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getCreateDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(CREATEDATE);
  }
  
  public com.runwaysdk.system.SingleActor getCreatedBy()
  {
    if (getValue(CREATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(CREATEDBY));
    }
  }
  
  public String getCreatedByOid()
  {
    return getValue(CREATEDBY);
  }
  
  public void validateCreatedBy()
  {
    this.validateAttribute(CREATEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getCreatedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(CREATEDBY);
  }
  
  public com.runwaysdk.system.metadata.MdDomain getEntityDomain()
  {
    if (getValue(ENTITYDOMAIN).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdDomain.get(getValue(ENTITYDOMAIN));
    }
  }
  
  public String getEntityDomainOid()
  {
    return getValue(ENTITYDOMAIN);
  }
  
  public void validateEntityDomain()
  {
    this.validateAttribute(ENTITYDOMAIN);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getEntityDomainMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(ENTITYDOMAIN);
  }
  
  public void setEntityDomain(com.runwaysdk.system.metadata.MdDomain value)
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
  
  public void setEntityDomainId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(ENTITYDOMAIN, "");
    }
    else
    {
      setValue(ENTITYDOMAIN, oid);
    }
  }
  
  public net.geoprism.registry.graph.HierarchicalRelationshipType getGraphHierarchy()
  {
    return (net.geoprism.registry.graph.HierarchicalRelationshipType)com.runwaysdk.business.graph.VertexObject.get("net.geoprism.registry.graph.HierarchicalRelationshipType", getValue(GRAPHHIERARCHY));
  }
  
  public void validateGraphHierarchy()
  {
    this.validateAttribute(GRAPHHIERARCHY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF getGraphHierarchyMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF)mdClassIF.definesAttribute(GRAPHHIERARCHY);
  }
  
  public void setGraphHierarchy(net.geoprism.registry.graph.HierarchicalRelationshipType value)
  {
    if(value == null)
    {
      setValue(GRAPHHIERARCHY, "");
    }
    else
    {
      setValue(GRAPHHIERARCHY, value.getOid());
    }
  }
  
  public Boolean getIsImport()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(ISIMPORT));
  }
  
  public void validateIsImport()
  {
    this.validateAttribute(ISIMPORT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF getIsImportMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF)mdClassIF.definesAttribute(ISIMPORT);
  }
  
  public void setIsImport(Boolean value)
  {
    if(value == null)
    {
      setValue(ISIMPORT, "");
    }
    else
    {
      setValue(ISIMPORT, java.lang.Boolean.toString(value));
    }
  }
  
  public String getKeyName()
  {
    return getValue(KEYNAME);
  }
  
  public void validateKeyName()
  {
    this.validateAttribute(KEYNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getKeyNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(KEYNAME);
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
  
  public net.geoprism.registry.SynchronizationConfigLabel getLabel()
  {
    return (net.geoprism.registry.SynchronizationConfigLabel) label;
  }
  
  public void validateLabel()
  {
    this.validateAttribute(LABEL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLocalCharacterDAOIF getLabelMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLocalCharacterDAOIF)mdClassIF.definesAttribute(LABEL);
  }
  
  public java.util.Date getLastSynchDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(LASTSYNCHDATE));
  }
  
  public void validateLastSynchDate()
  {
    this.validateAttribute(LASTSYNCHDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getLastSynchDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(LASTSYNCHDATE);
  }
  
  public void setLastSynchDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(LASTSYNCHDATE, "");
    }
    else
    {
      setValue(LASTSYNCHDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATETIME_FORMAT).format(value));
    }
  }
  
  public java.util.Date getLastUpdateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(LASTUPDATEDATE));
  }
  
  public void validateLastUpdateDate()
  {
    this.validateAttribute(LASTUPDATEDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getLastUpdateDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(LASTUPDATEDATE);
  }
  
  public com.runwaysdk.system.SingleActor getLastUpdatedBy()
  {
    if (getValue(LASTUPDATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(LASTUPDATEDBY));
    }
  }
  
  public String getLastUpdatedByOid()
  {
    return getValue(LASTUPDATEDBY);
  }
  
  public void validateLastUpdatedBy()
  {
    this.validateAttribute(LASTUPDATEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getLastUpdatedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(LASTUPDATEDBY);
  }
  
  public com.runwaysdk.system.SingleActor getLockedBy()
  {
    if (getValue(LOCKEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(LOCKEDBY));
    }
  }
  
  public String getLockedByOid()
  {
    return getValue(LOCKEDBY);
  }
  
  public void validateLockedBy()
  {
    this.validateAttribute(LOCKEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getLockedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(LOCKEDBY);
  }
  
  public String getOid()
  {
    return getValue(OID);
  }
  
  public void validateOid()
  {
    this.validateAttribute(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public net.geoprism.registry.Organization getOrganization()
  {
    if (getValue(ORGANIZATION).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.Organization.get(getValue(ORGANIZATION));
    }
  }
  
  public String getOrganizationOid()
  {
    return getValue(ORGANIZATION);
  }
  
  public void validateOrganization()
  {
    this.validateAttribute(ORGANIZATION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getOrganizationMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(ORGANIZATION);
  }
  
  public void setOrganization(net.geoprism.registry.Organization value)
  {
    if(value == null)
    {
      setValue(ORGANIZATION, "");
    }
    else
    {
      setValue(ORGANIZATION, value.getOid());
    }
  }
  
  public void setOrganizationId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(ORGANIZATION, "");
    }
    else
    {
      setValue(ORGANIZATION, oid);
    }
  }
  
  public com.runwaysdk.system.Actor getOwner()
  {
    if (getValue(OWNER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.Actor.get(getValue(OWNER));
    }
  }
  
  public String getOwnerOid()
  {
    return getValue(OWNER);
  }
  
  public void validateOwner()
  {
    this.validateAttribute(OWNER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getOwnerMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(OWNER);
  }
  
  public void setOwner(com.runwaysdk.system.Actor value)
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
  
  public void setOwnerId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(OWNER, "");
    }
    else
    {
      setValue(OWNER, oid);
    }
  }
  
  public Long getSeq()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(SEQ));
  }
  
  public void validateSeq()
  {
    this.validateAttribute(SEQ);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getSeqMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(SEQ);
  }
  
  public String getSiteMaster()
  {
    return getValue(SITEMASTER);
  }
  
  public void validateSiteMaster()
  {
    this.validateAttribute(SITEMASTER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getSiteMasterMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(SITEMASTER);
  }
  
  public String getSystem()
  {
    return getValue(SYSTEM);
  }
  
  public void validateSystem()
  {
    this.validateAttribute(SYSTEM);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getSystemMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(SYSTEM);
  }
  
  public void setSystem(String value)
  {
    if(value == null)
    {
      setValue(SYSTEM, "");
    }
    else
    {
      setValue(SYSTEM, value);
    }
  }
  
  public String getType()
  {
    return getValue(TYPE);
  }
  
  public void validateType()
  {
    this.validateAttribute(TYPE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getTypeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.SynchronizationConfig.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(TYPE);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static SynchronizationConfigQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    SynchronizationConfigQuery query = new SynchronizationConfigQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static SynchronizationConfig get(String oid)
  {
    return (SynchronizationConfig) com.runwaysdk.business.Business.get(oid);
  }
  
  public static SynchronizationConfig getByKey(String key)
  {
    return (SynchronizationConfig) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static SynchronizationConfig lock(java.lang.String oid)
  {
    SynchronizationConfig _instance = SynchronizationConfig.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static SynchronizationConfig unlock(java.lang.String oid)
  {
    SynchronizationConfig _instance = SynchronizationConfig.get(oid);
    _instance.unlock();
    
    return _instance;
  }
  
  public String toString()
  {
    if (this.isNew())
    {
      return "New: "+ this.getClassDisplayLabel();
    }
    else
    {
      return super.toString();
    }
  }
}
