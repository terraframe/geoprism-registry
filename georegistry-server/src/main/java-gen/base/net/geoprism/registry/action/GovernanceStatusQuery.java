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
package net.geoprism.registry.action;

@com.runwaysdk.business.ClassSignature(hash = 1169186401)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to GovernanceStatus.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class GovernanceStatusQuery extends com.runwaysdk.system.EnumerationMasterQuery

{

  public GovernanceStatusQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public GovernanceStatusQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return net.geoprism.registry.action.GovernanceStatus.CLASS;
  }
  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends GovernanceStatus> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<GovernanceStatus>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * 
 **/

  public com.runwaysdk.query.Condition enum_AllGovernanceStatus()
  {
    com.runwaysdk.query.QueryFactory queryFactory = this.getQueryFactory();
    com.runwaysdk.business.RelationshipQuery relationshipQuery = queryFactory.relationshipQuery(com.runwaysdk.system.metadata.EnumerationAttributeItem.CLASS);

    com.runwaysdk.business.BusinessQuery businessQuery = queryFactory.businessQuery(com.runwaysdk.system.metadata.MdEnumeration.CLASS);
    com.runwaysdk.dataaccess.MdEnumerationDAOIF mdEnumerationIF = com.runwaysdk.dataaccess.metadata.MdEnumerationDAO.getMdEnumerationDAO(net.geoprism.registry.action.AllGovernanceStatus.CLASS); 
    businessQuery.WHERE(businessQuery.oid().EQ(mdEnumerationIF.getOid()));

    relationshipQuery.WHERE(relationshipQuery.hasParent(businessQuery));

    return this.getBusinessQuery().isChildIn(relationshipQuery);
  }


/**
 * 
 **/

  public com.runwaysdk.query.Condition notEnum_AllGovernanceStatus()
  {
    com.runwaysdk.query.QueryFactory queryFactory = this.getQueryFactory();
    com.runwaysdk.business.RelationshipQuery relationshipQuery = queryFactory.relationshipQuery(com.runwaysdk.system.metadata.EnumerationAttributeItem.CLASS);

    com.runwaysdk.business.BusinessQuery businessQuery = queryFactory.businessQuery(com.runwaysdk.system.metadata.MdEnumeration.CLASS);
    com.runwaysdk.dataaccess.MdEnumerationDAOIF mdEnumerationIF = com.runwaysdk.dataaccess.metadata.MdEnumerationDAO.getMdEnumerationDAO(net.geoprism.registry.action.AllGovernanceStatus.CLASS); 
    businessQuery.WHERE(businessQuery.oid().EQ(mdEnumerationIF.getOid()));

    relationshipQuery.WHERE(relationshipQuery.hasParent(businessQuery));

    return this.getBusinessQuery().isNotChildIn(relationshipQuery);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface GovernanceStatusQueryReferenceIF extends com.runwaysdk.system.EnumerationMasterQuery.EnumerationMasterQueryReferenceIF
  {


    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.action.GovernanceStatus governanceStatus);

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.action.GovernanceStatus governanceStatus);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class GovernanceStatusQueryReference extends com.runwaysdk.system.EnumerationMasterQuery.EnumerationMasterQueryReference
 implements GovernanceStatusQueryReferenceIF

  {

  public GovernanceStatusQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.action.GovernanceStatus governanceStatus)
    {
      if(governanceStatus == null) return this.EQ((java.lang.String)null);
      return this.EQ(governanceStatus.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.action.GovernanceStatus governanceStatus)
    {
      if(governanceStatus == null) return this.NE((java.lang.String)null);
      return this.NE(governanceStatus.getOid());
    }

  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as an enumeration.
 **/
  public interface GovernanceStatusQueryEnumerationIF extends com.runwaysdk.system.EnumerationMasterQuery.EnumerationMasterQueryEnumerationIF
  {


  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as an enumeration.
 **/
  public static class GovernanceStatusQueryEnumeration extends com.runwaysdk.system.EnumerationMasterQuery.EnumerationMasterQueryEnumeration
 implements GovernanceStatusQueryEnumerationIF
  {

  public GovernanceStatusQueryEnumeration(com.runwaysdk.dataaccess.MdAttributeEnumerationDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdEnumerationTableName,com.runwaysdk.dataaccess.MdBusinessDAOIF masterMdBusinessIF, String masterTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdEnumerationTableName, masterMdBusinessIF, masterTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }

  }

/**
 * Specifies type safe query methods for the enumeration net.geoprism.registry.action.AllGovernanceStatus.
 * This type is used when a join is performed on this class as an enumeration.
 **/
  public interface AllGovernanceStatusQueryIF extends GovernanceStatusQueryEnumerationIF  {

    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus);
    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus);
    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus);
    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus);
    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus);
  }

/**
 * Implements type safe query methods for the enumeration net.geoprism.registry.action.AllGovernanceStatus.
 * This type is used when a join is performed on this class as an enumeration.
 **/
  public static class AllGovernanceStatusQuery extends GovernanceStatusQueryEnumeration implements  AllGovernanceStatusQueryIF
  {
  public AllGovernanceStatusQuery(com.runwaysdk.dataaccess.MdAttributeEnumerationDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdEnumerationTableName,com.runwaysdk.dataaccess.MdBusinessDAOIF masterMdBusinessIF, String masterTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdEnumerationTableName, masterMdBusinessIF, masterTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }

    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus)  {

      String[] enumIdArray = new String[allGovernanceStatus.length]; 

      for (int i=0; i<allGovernanceStatus.length; i++)
      {
        enumIdArray[i] = allGovernanceStatus[i].getOid();
      }

      return this.containsAny(enumIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus)  {

      String[] enumIdArray = new String[allGovernanceStatus.length]; 

      for (int i=0; i<allGovernanceStatus.length; i++)
      {
        enumIdArray[i] = allGovernanceStatus[i].getOid();
      }

      return this.notContainsAny(enumIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus)  {

      String[] enumIdArray = new String[allGovernanceStatus.length]; 

      for (int i=0; i<allGovernanceStatus.length; i++)
      {
        enumIdArray[i] = allGovernanceStatus[i].getOid();
      }

      return this.containsAll(enumIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus)  {

      String[] enumIdArray = new String[allGovernanceStatus.length]; 

      for (int i=0; i<allGovernanceStatus.length; i++)
      {
        enumIdArray[i] = allGovernanceStatus[i].getOid();
      }

      return this.notContainsAll(enumIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.action.AllGovernanceStatus ... allGovernanceStatus)  {

      String[] enumIdArray = new String[allGovernanceStatus.length]; 

      for (int i=0; i<allGovernanceStatus.length; i++)
      {
        enumIdArray[i] = allGovernanceStatus[i].getOid();
      }

      return this.containsExactly(enumIdArray);
  }
  }
/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface GovernanceStatusQueryMultiReferenceIF extends com.runwaysdk.system.EnumerationMasterQuery.EnumerationMasterQueryMultiReferenceIF
  {


    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.action.GovernanceStatus ... governanceStatus);
    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.action.GovernanceStatus ... governanceStatus);
    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.action.GovernanceStatus ... governanceStatus);
    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.action.GovernanceStatus ... governanceStatus);
    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.action.GovernanceStatus ... governanceStatus);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class GovernanceStatusQueryMultiReference extends com.runwaysdk.system.EnumerationMasterQuery.EnumerationMasterQueryMultiReference
 implements GovernanceStatusQueryMultiReferenceIF

  {

  public GovernanceStatusQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.action.GovernanceStatus ... governanceStatus)  {

      String[] itemIdArray = new String[governanceStatus.length]; 

      for (int i=0; i<governanceStatus.length; i++)
      {
        itemIdArray[i] = governanceStatus[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.action.GovernanceStatus ... governanceStatus)  {

      String[] itemIdArray = new String[governanceStatus.length]; 

      for (int i=0; i<governanceStatus.length; i++)
      {
        itemIdArray[i] = governanceStatus[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.action.GovernanceStatus ... governanceStatus)  {

      String[] itemIdArray = new String[governanceStatus.length]; 

      for (int i=0; i<governanceStatus.length; i++)
      {
        itemIdArray[i] = governanceStatus[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.action.GovernanceStatus ... governanceStatus)  {

      String[] itemIdArray = new String[governanceStatus.length]; 

      for (int i=0; i<governanceStatus.length; i++)
      {
        itemIdArray[i] = governanceStatus[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.action.GovernanceStatus ... governanceStatus)  {

      String[] itemIdArray = new String[governanceStatus.length]; 

      for (int i=0; i<governanceStatus.length; i++)
      {
        itemIdArray[i] = governanceStatus[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  }
}
