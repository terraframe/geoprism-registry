package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 315976602)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to LabeledPropertyGraphTypeVersion.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class LabeledPropertyGraphTypeVersionBase extends com.runwaysdk.business.Business
{
  public final static String CLASS = "net.geoprism.registry.LabeledPropertyGraphTypeVersion";
  public final static java.lang.String CREATEDATE = "createDate";
  public final static java.lang.String CREATEDBY = "createdBy";
  public final static java.lang.String ENTITYDOMAIN = "entityDomain";
  public final static java.lang.String ENTRY = "entry";
  public final static java.lang.String FORDATE = "forDate";
  public final static java.lang.String GRAPHTYPE = "graphType";
  public final static java.lang.String KEYNAME = "keyName";
  public final static java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public final static java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public final static java.lang.String LOCKEDBY = "lockedBy";
  public final static java.lang.String OID = "oid";
  public final static java.lang.String OWNER = "owner";
  public final static java.lang.String PUBLISHDATE = "publishDate";
  public final static java.lang.String SEQ = "seq";
  public final static java.lang.String SITEMASTER = "siteMaster";
  public final static java.lang.String TYPE = "type";
  public final static java.lang.String VERSIONNUMBER = "versionNumber";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 315976602;
  
  public LabeledPropertyGraphTypeVersionBase()
  {
    super();
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
  
  public net.geoprism.registry.LabeledPropertyGraphTypeEntry getEntry()
  {
    if (getValue(ENTRY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.LabeledPropertyGraphTypeEntry.get(getValue(ENTRY));
    }
  }
  
  public String getEntryOid()
  {
    return getValue(ENTRY);
  }
  
  public void validateEntry()
  {
    this.validateAttribute(ENTRY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getEntryMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(ENTRY);
  }
  
  public void setEntry(net.geoprism.registry.LabeledPropertyGraphTypeEntry value)
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
  
  public void setEntryId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(ENTRY, "");
    }
    else
    {
      setValue(ENTRY, oid);
    }
  }
  
  public java.util.Date getForDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(FORDATE));
  }
  
  public void validateForDate()
  {
    this.validateAttribute(FORDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getForDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(FORDATE);
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
  
  public net.geoprism.registry.LabeledPropertyGraphType getGraphType()
  {
    if (getValue(GRAPHTYPE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.LabeledPropertyGraphType.get(getValue(GRAPHTYPE));
    }
  }
  
  public String getGraphTypeOid()
  {
    return getValue(GRAPHTYPE);
  }
  
  public void validateGraphType()
  {
    this.validateAttribute(GRAPHTYPE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getGraphTypeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(GRAPHTYPE);
  }
  
  public void setGraphType(net.geoprism.registry.LabeledPropertyGraphType value)
  {
    if(value == null)
    {
      setValue(GRAPHTYPE, "");
    }
    else
    {
      setValue(GRAPHTYPE, value.getOid());
    }
  }
  
  public void setGraphTypeId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(GRAPHTYPE, "");
    }
    else
    {
      setValue(GRAPHTYPE, oid);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
  
  public java.util.Date getPublishDate()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(PUBLISHDATE));
  }
  
  public void validatePublishDate()
  {
    this.validateAttribute(PUBLISHDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateDAOIF getPublishDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateDAOIF)mdClassIF.definesAttribute(PUBLISHDATE);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(SITEMASTER);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(TYPE);
  }
  
  public Integer getVersionNumber()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(VERSIONNUMBER));
  }
  
  public void validateVersionNumber()
  {
    this.validateAttribute(VERSIONNUMBER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF getVersionNumberMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.LabeledPropertyGraphTypeVersion.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF)mdClassIF.definesAttribute(VERSIONNUMBER);
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
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static LabeledPropertyGraphTypeVersionQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    LabeledPropertyGraphTypeVersionQuery query = new LabeledPropertyGraphTypeVersionQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public net.geoprism.registry.action.GraphHasEdge addEdges(net.geoprism.registry.LabeledPropertyGraphEdge labeledPropertyGraphEdge)
  {
    return (net.geoprism.registry.action.GraphHasEdge) addChild(labeledPropertyGraphEdge, net.geoprism.registry.action.GraphHasEdge.CLASS);
  }
  
  public void removeEdges(net.geoprism.registry.LabeledPropertyGraphEdge labeledPropertyGraphEdge)
  {
    removeAllChildren(labeledPropertyGraphEdge, net.geoprism.registry.action.GraphHasEdge.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends net.geoprism.registry.LabeledPropertyGraphEdge> getAllEdges()
  {
    return (com.runwaysdk.query.OIterator<? extends net.geoprism.registry.LabeledPropertyGraphEdge>) getChildren(net.geoprism.registry.action.GraphHasEdge.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends net.geoprism.registry.action.GraphHasEdge> getAllEdgesRel()
  {
    return (com.runwaysdk.query.OIterator<? extends net.geoprism.registry.action.GraphHasEdge>) getChildRelationships(net.geoprism.registry.action.GraphHasEdge.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends net.geoprism.registry.action.GraphHasEdge> getEdgesRel(net.geoprism.registry.LabeledPropertyGraphEdge labeledPropertyGraphEdge)
  {
    return (com.runwaysdk.query.OIterator<? extends net.geoprism.registry.action.GraphHasEdge>) getRelationshipsWithChild(labeledPropertyGraphEdge, net.geoprism.registry.action.GraphHasEdge.CLASS);
  }
  
  public net.geoprism.registry.action.GraphHasVertex addVertices(net.geoprism.registry.LabeledPropertyGraphVertex labeledPropertyGraphVertex)
  {
    return (net.geoprism.registry.action.GraphHasVertex) addChild(labeledPropertyGraphVertex, net.geoprism.registry.action.GraphHasVertex.CLASS);
  }
  
  public void removeVertices(net.geoprism.registry.LabeledPropertyGraphVertex labeledPropertyGraphVertex)
  {
    removeAllChildren(labeledPropertyGraphVertex, net.geoprism.registry.action.GraphHasVertex.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends net.geoprism.registry.LabeledPropertyGraphVertex> getAllVertices()
  {
    return (com.runwaysdk.query.OIterator<? extends net.geoprism.registry.LabeledPropertyGraphVertex>) getChildren(net.geoprism.registry.action.GraphHasVertex.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends net.geoprism.registry.action.GraphHasVertex> getAllVerticesRel()
  {
    return (com.runwaysdk.query.OIterator<? extends net.geoprism.registry.action.GraphHasVertex>) getChildRelationships(net.geoprism.registry.action.GraphHasVertex.CLASS);
  }
  
  @SuppressWarnings("unchecked")
  public com.runwaysdk.query.OIterator<? extends net.geoprism.registry.action.GraphHasVertex> getVerticesRel(net.geoprism.registry.LabeledPropertyGraphVertex labeledPropertyGraphVertex)
  {
    return (com.runwaysdk.query.OIterator<? extends net.geoprism.registry.action.GraphHasVertex>) getRelationshipsWithChild(labeledPropertyGraphVertex, net.geoprism.registry.action.GraphHasVertex.CLASS);
  }
  
  public static LabeledPropertyGraphTypeVersion get(String oid)
  {
    return (LabeledPropertyGraphTypeVersion) com.runwaysdk.business.Business.get(oid);
  }
  
  public static LabeledPropertyGraphTypeVersion getByKey(String key)
  {
    return (LabeledPropertyGraphTypeVersion) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public java.lang.String publish()
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.registry.LabeledPropertyGraphTypeVersion.java";
    throw new com.runwaysdk.dataaccess.metadata.ForbiddenMethodException(msg);
  }
  
  public static final java.lang.String publish(java.lang.String oid)
  {
    LabeledPropertyGraphTypeVersion _instance = LabeledPropertyGraphTypeVersion.get(oid);
    return _instance.publish();
  }
  
  public void remove()
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.registry.LabeledPropertyGraphTypeVersion.java";
    throw new com.runwaysdk.dataaccess.metadata.ForbiddenMethodException(msg);
  }
  
  public static final void remove(java.lang.String oid)
  {
    LabeledPropertyGraphTypeVersion _instance = LabeledPropertyGraphTypeVersion.get(oid);
    _instance.remove();
  }
  
  public static LabeledPropertyGraphTypeVersion lock(java.lang.String oid)
  {
    LabeledPropertyGraphTypeVersion _instance = LabeledPropertyGraphTypeVersion.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static LabeledPropertyGraphTypeVersion unlock(java.lang.String oid)
  {
    LabeledPropertyGraphTypeVersion _instance = LabeledPropertyGraphTypeVersion.get(oid);
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
