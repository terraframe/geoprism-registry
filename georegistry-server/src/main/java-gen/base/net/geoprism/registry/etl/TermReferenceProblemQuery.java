package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = -1338937270)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to TermReferenceProblem.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class TermReferenceProblemQuery extends net.geoprism.registry.etl.ValidationProblemQuery

{

  public TermReferenceProblemQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public TermReferenceProblemQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return net.geoprism.registry.etl.TermReferenceProblem.CLASS;
  }
  public com.runwaysdk.query.SelectableChar getAttributeCode()
  {
    return getAttributeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTECODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getAttributeLabel()
  {
    return getAttributeLabel(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTELABEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTELABEL, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode()
  {
    return getGeoObjectTypeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.GEOOBJECTTYPECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.GEOOBJECTTYPECODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getLabel()
  {
    return getLabel(null);

  }
 
  public com.runwaysdk.query.SelectableChar getLabel(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.LABEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getLabel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.LABEL, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getParentCode()
  {
    return getParentCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getParentCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.PARENTCODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getParentCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.PARENTCODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getTypeCode()
  {
    return getTypeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getTypeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.TYPECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getTypeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.etl.TermReferenceProblem.TYPECODE, alias, displayLabel);

  }
  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends TermReferenceProblem> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<TermReferenceProblem>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface TermReferenceProblemQueryReferenceIF extends net.geoprism.registry.etl.ValidationProblemQuery.ValidationProblemQueryReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getAttributeCode();
    public com.runwaysdk.query.SelectableChar getAttributeCode(String alias);
    public com.runwaysdk.query.SelectableChar getAttributeCode(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getAttributeLabel();
    public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias);
    public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode();
    public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias);
    public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getLabel();
    public com.runwaysdk.query.SelectableChar getLabel(String alias);
    public com.runwaysdk.query.SelectableChar getLabel(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getParentCode();
    public com.runwaysdk.query.SelectableChar getParentCode(String alias);
    public com.runwaysdk.query.SelectableChar getParentCode(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getTypeCode();
    public com.runwaysdk.query.SelectableChar getTypeCode(String alias);
    public com.runwaysdk.query.SelectableChar getTypeCode(String alias, String displayLabel);

    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.etl.TermReferenceProblem termReferenceProblem);

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.etl.TermReferenceProblem termReferenceProblem);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class TermReferenceProblemQueryReference extends net.geoprism.registry.etl.ValidationProblemQuery.ValidationProblemQueryReference
 implements TermReferenceProblemQueryReferenceIF

  {

  public TermReferenceProblemQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.etl.TermReferenceProblem termReferenceProblem)
    {
      if(termReferenceProblem == null) return this.EQ((java.lang.String)null);
      return this.EQ(termReferenceProblem.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.etl.TermReferenceProblem termReferenceProblem)
    {
      if(termReferenceProblem == null) return this.NE((java.lang.String)null);
      return this.NE(termReferenceProblem.getOid());
    }

  public com.runwaysdk.query.SelectableChar getAttributeCode()
  {
    return getAttributeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTECODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getAttributeLabel()
  {
    return getAttributeLabel(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTELABEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTELABEL, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode()
  {
    return getGeoObjectTypeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.GEOOBJECTTYPECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.GEOOBJECTTYPECODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getLabel()
  {
    return getLabel(null);

  }
 
  public com.runwaysdk.query.SelectableChar getLabel(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.LABEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getLabel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.LABEL, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getParentCode()
  {
    return getParentCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getParentCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.PARENTCODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getParentCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.PARENTCODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getTypeCode()
  {
    return getTypeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getTypeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.TYPECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getTypeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.TYPECODE, alias, displayLabel);

  }
  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface TermReferenceProblemQueryMultiReferenceIF extends net.geoprism.registry.etl.ValidationProblemQuery.ValidationProblemQueryMultiReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getAttributeCode();
    public com.runwaysdk.query.SelectableChar getAttributeCode(String alias);
    public com.runwaysdk.query.SelectableChar getAttributeCode(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getAttributeLabel();
    public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias);
    public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode();
    public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias);
    public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getLabel();
    public com.runwaysdk.query.SelectableChar getLabel(String alias);
    public com.runwaysdk.query.SelectableChar getLabel(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getParentCode();
    public com.runwaysdk.query.SelectableChar getParentCode(String alias);
    public com.runwaysdk.query.SelectableChar getParentCode(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getTypeCode();
    public com.runwaysdk.query.SelectableChar getTypeCode(String alias);
    public com.runwaysdk.query.SelectableChar getTypeCode(String alias, String displayLabel);

    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem);
    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem);
    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem);
    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem);
    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class TermReferenceProblemQueryMultiReference extends net.geoprism.registry.etl.ValidationProblemQuery.ValidationProblemQueryMultiReference
 implements TermReferenceProblemQueryMultiReferenceIF

  {

  public TermReferenceProblemQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem)  {

      String[] itemIdArray = new String[termReferenceProblem.length]; 

      for (int i=0; i<termReferenceProblem.length; i++)
      {
        itemIdArray[i] = termReferenceProblem[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem)  {

      String[] itemIdArray = new String[termReferenceProblem.length]; 

      for (int i=0; i<termReferenceProblem.length; i++)
      {
        itemIdArray[i] = termReferenceProblem[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem)  {

      String[] itemIdArray = new String[termReferenceProblem.length]; 

      for (int i=0; i<termReferenceProblem.length; i++)
      {
        itemIdArray[i] = termReferenceProblem[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem)  {

      String[] itemIdArray = new String[termReferenceProblem.length]; 

      for (int i=0; i<termReferenceProblem.length; i++)
      {
        itemIdArray[i] = termReferenceProblem[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.etl.TermReferenceProblem ... termReferenceProblem)  {

      String[] itemIdArray = new String[termReferenceProblem.length]; 

      for (int i=0; i<termReferenceProblem.length; i++)
      {
        itemIdArray[i] = termReferenceProblem[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  public com.runwaysdk.query.SelectableChar getAttributeCode()
  {
    return getAttributeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTECODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getAttributeLabel()
  {
    return getAttributeLabel(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTELABEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeLabel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.ATTRIBUTELABEL, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode()
  {
    return getGeoObjectTypeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.GEOOBJECTTYPECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getGeoObjectTypeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.GEOOBJECTTYPECODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getLabel()
  {
    return getLabel(null);

  }
 
  public com.runwaysdk.query.SelectableChar getLabel(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.LABEL, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getLabel(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.LABEL, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getParentCode()
  {
    return getParentCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getParentCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.PARENTCODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getParentCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.PARENTCODE, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getTypeCode()
  {
    return getTypeCode(null);

  }
 
  public com.runwaysdk.query.SelectableChar getTypeCode(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.TYPECODE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getTypeCode(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.etl.TermReferenceProblem.TYPECODE, alias, displayLabel);

  }
  }
}
