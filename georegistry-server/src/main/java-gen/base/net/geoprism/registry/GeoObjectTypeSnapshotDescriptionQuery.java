package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -2009865605)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to GeoObjectTypeSnapshotDescription.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class GeoObjectTypeSnapshotDescriptionQuery extends com.runwaysdk.query.GeneratedStructQuery
{

  public GeoObjectTypeSnapshotDescriptionQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
     super();
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.StructQuery structQuery = componentQueryFactory.structQuery(this.getClassType());

       this.setStructQuery(structQuery);
    }
  }

  public GeoObjectTypeSnapshotDescriptionQuery(com.runwaysdk.query.ValueQuery valueQuery)
  {
     super();
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.StructQuery structQuery = new com.runwaysdk.business.StructQuery(valueQuery, this.getClassType());

       this.setStructQuery(structQuery);
    }
  }

  public String getClassType()
  {
    return net.geoprism.registry.GeoObjectTypeSnapshotDescription.CLASS;
  }
  public com.runwaysdk.query.SelectableChar getDefaultLocale()
  {
    return getDefaultLocale(null);

  }
 
  public com.runwaysdk.query.SelectableChar getDefaultLocale(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.DEFAULTLOCALE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getDefaultLocale(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.DEFAULTLOCALE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getKeyName()
  {
    return getKeyName(null);

  }
 
  public com.runwaysdk.query.SelectableChar getKeyName(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.KEYNAME, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getKeyName(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.KEYNAME, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableUUID getOid()
  {
    return getOid(null);

  }
 
  public com.runwaysdk.query.SelectableUUID getOid(String alias)
  {
    return (com.runwaysdk.query.SelectableUUID)this.getComponentQuery().get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.OID, alias, null);

  }
 
  public com.runwaysdk.query.SelectableUUID getOid(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableUUID)this.getComponentQuery().get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.OID, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getSiteMaster()
  {
    return getSiteMaster(null);

  }
 
  public com.runwaysdk.query.SelectableChar getSiteMaster(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.SITEMASTER, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getSiteMaster(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.SITEMASTER, alias, displayLabel);

  }
  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends GeoObjectTypeSnapshotDescription> getIterator()
  {
    this.checkNotUsedInValueQuery();
    String sqlStmt;
    if (_limit != null && _skip != null)
    {
      sqlStmt = this.getComponentQuery().getSQL(_limit, _skip);
    }
    else
    {
      sqlStmt = this.getComponentQuery().getSQL();
    }
    java.util.Map<String, com.runwaysdk.query.ColumnInfo> columnInfoMap = this.getComponentQuery().getColumnInfoMap();

    java.sql.ResultSet results = com.runwaysdk.dataaccess.database.Database.query(sqlStmt);
    return new com.runwaysdk.business.StructIterator<GeoObjectTypeSnapshotDescription>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a struct attribute.
 **/
  public interface GeoObjectTypeSnapshotDescriptionQueryStructIF extends com.runwaysdk.query.AttributeLocalIF  {

    public com.runwaysdk.query.SelectableChar getDefaultLocale();
    public com.runwaysdk.query.SelectableChar getDefaultLocale(String alias);
    public com.runwaysdk.query.SelectableChar getDefaultLocale(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getKeyName();
    public com.runwaysdk.query.SelectableChar getKeyName(String alias);
    public com.runwaysdk.query.SelectableChar getKeyName(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableUUID getOid();
    public com.runwaysdk.query.SelectableUUID getOid(String alias);
    public com.runwaysdk.query.SelectableUUID getOid(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getSiteMaster();
    public com.runwaysdk.query.SelectableChar getSiteMaster(String alias);
    public com.runwaysdk.query.SelectableChar getSiteMaster(String alias, String displayLabel);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a struct attribute.
 **/
  public static class GeoObjectTypeSnapshotDescriptionQueryStruct extends com.runwaysdk.query.AttributeLocal implements GeoObjectTypeSnapshotDescriptionQueryStructIF  {

  public GeoObjectTypeSnapshotDescriptionQueryStruct(com.runwaysdk.dataaccess.MdAttributeLocalDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdLocalStructDAOIF mdStructIF, String structTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdStructIF, structTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }

  public com.runwaysdk.query.SelectableChar getDefaultLocale()
  {
    return getDefaultLocale(null);

  }
 
  public com.runwaysdk.query.SelectableChar getDefaultLocale(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.DEFAULTLOCALE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getDefaultLocale(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.DEFAULTLOCALE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getKeyName()
  {
    return getKeyName(null);

  }
 
  public com.runwaysdk.query.SelectableChar getKeyName(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.KEYNAME, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getKeyName(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.KEYNAME, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableUUID getOid()
  {
    return getOid(null);

  }
 
  public com.runwaysdk.query.SelectableUUID getOid(String alias)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.OID, alias, null);

  }
 
  public com.runwaysdk.query.SelectableUUID getOid(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.OID, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getSiteMaster()
  {
    return getSiteMaster(null);

  }
 
  public com.runwaysdk.query.SelectableChar getSiteMaster(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.SITEMASTER, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getSiteMaster(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.GeoObjectTypeSnapshotDescription.SITEMASTER, alias, displayLabel);

  }
  }
}
