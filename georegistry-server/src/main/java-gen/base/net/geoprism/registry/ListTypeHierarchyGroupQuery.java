package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -2087388731)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ListTypeHierarchyGroup.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class ListTypeHierarchyGroupQuery extends net.geoprism.registry.ListTypeGroupQuery

{

  public ListTypeHierarchyGroupQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public ListTypeHierarchyGroupQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return net.geoprism.registry.ListTypeHierarchyGroup.CLASS;
  }
  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy()
  {
    return getHierarchy(null);

  }
 
  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY);

    return (net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY, mdAttributeIF, this, alias, null);

  }
 
  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias, String displayLabel)
  {

    com.runwaysdk.dataaccess.MdAttributeDAOIF mdAttributeIF = this.getComponentQuery().getMdAttributeROfromMap(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY);

    return (net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF)this.getComponentQuery().internalAttributeFactory(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY, mdAttributeIF, this, alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY)) 
    {
       return new net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
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
  public com.runwaysdk.query.OIterator<? extends ListTypeHierarchyGroup> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<ListTypeHierarchyGroup>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ListTypeHierarchyGroupQueryReferenceIF extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryReferenceIF
  {

    public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy();
    public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias);
    public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias, String displayLabel);

    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.ListTypeHierarchyGroup listTypeHierarchyGroup);

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.ListTypeHierarchyGroup listTypeHierarchyGroup);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ListTypeHierarchyGroupQueryReference extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryReference
 implements ListTypeHierarchyGroupQueryReferenceIF

  {

  public ListTypeHierarchyGroupQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.ListTypeHierarchyGroup listTypeHierarchyGroup)
    {
      if(listTypeHierarchyGroup == null) return this.EQ((java.lang.String)null);
      return this.EQ(listTypeHierarchyGroup.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.ListTypeHierarchyGroup listTypeHierarchyGroup)
    {
      if(listTypeHierarchyGroup == null) return this.NE((java.lang.String)null);
      return this.NE(listTypeHierarchyGroup.getOid());
    }

  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy()
  {
    return getHierarchy(null);

  }
 
  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias)
  {
    return (net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF)this.get(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY, alias, null);

  }
 
  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias, String displayLabel)
  {
    return (net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF)this.get(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY,  alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY)) 
    {
       return new net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
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
  public interface ListTypeHierarchyGroupQueryMultiReferenceIF extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryMultiReferenceIF
  {

    public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy();
    public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias);
    public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias, String displayLabel);

    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup);
    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup);
    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup);
    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup);
    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ListTypeHierarchyGroupQueryMultiReference extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryMultiReference
 implements ListTypeHierarchyGroupQueryMultiReferenceIF

  {

  public ListTypeHierarchyGroupQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup)  {

      String[] itemIdArray = new String[listTypeHierarchyGroup.length]; 

      for (int i=0; i<listTypeHierarchyGroup.length; i++)
      {
        itemIdArray[i] = listTypeHierarchyGroup[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup)  {

      String[] itemIdArray = new String[listTypeHierarchyGroup.length]; 

      for (int i=0; i<listTypeHierarchyGroup.length; i++)
      {
        itemIdArray[i] = listTypeHierarchyGroup[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup)  {

      String[] itemIdArray = new String[listTypeHierarchyGroup.length]; 

      for (int i=0; i<listTypeHierarchyGroup.length; i++)
      {
        itemIdArray[i] = listTypeHierarchyGroup[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup)  {

      String[] itemIdArray = new String[listTypeHierarchyGroup.length]; 

      for (int i=0; i<listTypeHierarchyGroup.length; i++)
      {
        itemIdArray[i] = listTypeHierarchyGroup[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.ListTypeHierarchyGroup ... listTypeHierarchyGroup)  {

      String[] itemIdArray = new String[listTypeHierarchyGroup.length]; 

      for (int i=0; i<listTypeHierarchyGroup.length; i++)
      {
        itemIdArray[i] = listTypeHierarchyGroup[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy()
  {
    return getHierarchy(null);

  }
 
  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias)
  {
    return (net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF)this.get(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY, alias, null);

  }
 
  public net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF getHierarchy(String alias, String displayLabel)
  {
    return (net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReferenceIF)this.get(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY,  alias, displayLabel);

  }
  protected com.runwaysdk.query.AttributeReference referenceFactory( com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias,  com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String userDefinedAlias, String userDefinedDisplayLabel)
  {
    String name = mdAttributeIF.definesAttribute();
    
    if (name.equals(net.geoprism.registry.ListTypeHierarchyGroup.HIERARCHY)) 
    {
       return new net.geoprism.registry.HierarchicalRelationshipTypeQuery.HierarchicalRelationshipTypeQueryReference((com.runwaysdk.dataaccess.MdAttributeRefDAOIF)mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
    else 
    {
      return super.referenceFactory(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, userDefinedAlias, userDefinedDisplayLabel);
    }
  }

  }
}