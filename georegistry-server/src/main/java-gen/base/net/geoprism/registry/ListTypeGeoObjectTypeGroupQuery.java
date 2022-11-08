package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 700443025)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ListTypeGeoObjectTypeGroup.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class ListTypeGeoObjectTypeGroupQuery extends net.geoprism.registry.ListTypeGroupQuery

{

  public ListTypeGeoObjectTypeGroupQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public ListTypeGeoObjectTypeGroupQuery(com.runwaysdk.query.ValueQuery valueQuery)
  {
    super(valueQuery);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = new com.runwaysdk.business.BusinessQuery(valueQuery, this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public String getClassType()
  {
    return net.geoprism.registry.ListTypeGeoObjectTypeGroup.CLASS;
  }
  public com.runwaysdk.query.SelectableInteger getLevel()
  {
    return getLevel(null);

  }
 
  public com.runwaysdk.query.SelectableInteger getLevel(String alias)
  {
    return (com.runwaysdk.query.SelectableInteger)this.getComponentQuery().get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.LEVEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableInteger getLevel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableInteger)this.getComponentQuery().get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.LEVEL, alias, displayLabel);

  }
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal()
  {
    return getUniversal(null);

  }
 
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL);

    return (com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL, mdAttributeIF, this, alias, null);

  }
 
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias, String displayLabel)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL);

    return (com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL, mdAttributeIF, this, alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL)) 
    {
       return new com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else 
    {
      return super.referenceFactory(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
  }

  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends ListTypeGeoObjectTypeGroup> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<ListTypeGeoObjectTypeGroup>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ListTypeGeoObjectTypeGroupQueryReferenceIF extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryReferenceIF
  {

    public com.runwaysdk.query.SelectableInteger getLevel();
    public com.runwaysdk.query.SelectableInteger getLevel(String alias);
    public com.runwaysdk.query.SelectableInteger getLevel(String alias, String displayLabel);
    public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal();
    public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias);
    public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias, String displayLabel);

    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.ListTypeGeoObjectTypeGroup listTypeGeoObjectTypeGroup);

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.ListTypeGeoObjectTypeGroup listTypeGeoObjectTypeGroup);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ListTypeGeoObjectTypeGroupQueryReference extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryReference
 implements ListTypeGeoObjectTypeGroupQueryReferenceIF

  {

  public ListTypeGeoObjectTypeGroupQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.ListTypeGeoObjectTypeGroup listTypeGeoObjectTypeGroup)
    {
      if(listTypeGeoObjectTypeGroup == null) return this.EQ((java.lang.String)null);
      return this.EQ(listTypeGeoObjectTypeGroup.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.ListTypeGeoObjectTypeGroup listTypeGeoObjectTypeGroup)
    {
      if(listTypeGeoObjectTypeGroup == null) return this.NE((java.lang.String)null);
      return this.NE(listTypeGeoObjectTypeGroup.getOid());
    }

  public com.runwaysdk.query.SelectableInteger getLevel()
  {
    return getLevel(null);

  }
 
  public com.runwaysdk.query.SelectableInteger getLevel(String alias)
  {
    return (com.runwaysdk.query.SelectableInteger)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.LEVEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableInteger getLevel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableInteger)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.LEVEL, alias, displayLabel);

  }
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal()
  {
    return getUniversal(null);

  }
 
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias)
  {
    return (com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL, alias, null);

  }
 
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias, String displayLabel)
  {
    return (com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL,  alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL)) 
    {
       return new com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else 
    {
      return super.referenceFactory(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
  }

  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ListTypeGeoObjectTypeGroupQueryMultiReferenceIF extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryMultiReferenceIF
  {

    public com.runwaysdk.query.SelectableInteger getLevel();
    public com.runwaysdk.query.SelectableInteger getLevel(String alias);
    public com.runwaysdk.query.SelectableInteger getLevel(String alias, String displayLabel);
    public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal();
    public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias);
    public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias, String displayLabel);

    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup);
    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup);
    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup);
    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup);
    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ListTypeGeoObjectTypeGroupQueryMultiReference extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryMultiReference
 implements ListTypeGeoObjectTypeGroupQueryMultiReferenceIF

  {

  public ListTypeGeoObjectTypeGroupQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup)  {

      String[] itemIdArray = new String[listTypeGeoObjectTypeGroup.length]; 

      for (int i=0; i<listTypeGeoObjectTypeGroup.length; i++)
      {
        itemIdArray[i] = listTypeGeoObjectTypeGroup[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup)  {

      String[] itemIdArray = new String[listTypeGeoObjectTypeGroup.length]; 

      for (int i=0; i<listTypeGeoObjectTypeGroup.length; i++)
      {
        itemIdArray[i] = listTypeGeoObjectTypeGroup[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup)  {

      String[] itemIdArray = new String[listTypeGeoObjectTypeGroup.length]; 

      for (int i=0; i<listTypeGeoObjectTypeGroup.length; i++)
      {
        itemIdArray[i] = listTypeGeoObjectTypeGroup[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup)  {

      String[] itemIdArray = new String[listTypeGeoObjectTypeGroup.length]; 

      for (int i=0; i<listTypeGeoObjectTypeGroup.length; i++)
      {
        itemIdArray[i] = listTypeGeoObjectTypeGroup[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.ListTypeGeoObjectTypeGroup ... listTypeGeoObjectTypeGroup)  {

      String[] itemIdArray = new String[listTypeGeoObjectTypeGroup.length]; 

      for (int i=0; i<listTypeGeoObjectTypeGroup.length; i++)
      {
        itemIdArray[i] = listTypeGeoObjectTypeGroup[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  public com.runwaysdk.query.SelectableInteger getLevel()
  {
    return getLevel(null);

  }
 
  public com.runwaysdk.query.SelectableInteger getLevel(String alias)
  {
    return (com.runwaysdk.query.SelectableInteger)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.LEVEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableInteger getLevel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableInteger)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.LEVEL, alias, displayLabel);

  }
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal()
  {
    return getUniversal(null);

  }
 
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias)
  {
    return (com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL, alias, null);

  }
 
  public com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF getUniversal(String alias, String displayLabel)
  {
    return (com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReferenceIF)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL,  alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(net.geoprism.registry.ListTypeGeoObjectTypeGroup.UNIVERSAL)) 
    {
       return new com.runwaysdk.system.gis.geo.UniversalQuery.UniversalQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else 
    {
      return super.referenceFactory(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
  }

  }
}