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
package net.geoprism.registry.curation;

@com.runwaysdk.business.ClassSignature(hash = -320090734)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ListCurationJob.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class ListCurationJobQuery extends com.runwaysdk.system.scheduler.ExecutableJobQuery

{

  public ListCurationJobQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public ListCurationJobQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return net.geoprism.registry.curation.ListCurationJob.CLASS;
  }
  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends ListCurationJob> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<ListCurationJob>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ListCurationJobQueryReferenceIF extends com.runwaysdk.system.scheduler.ExecutableJobQuery.ExecutableJobQueryReferenceIF
  {


    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.curation.ListCurationJob listCurationJob);

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.curation.ListCurationJob listCurationJob);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ListCurationJobQueryReference extends com.runwaysdk.system.scheduler.ExecutableJobQuery.ExecutableJobQueryReference
 implements ListCurationJobQueryReferenceIF

  {

  public ListCurationJobQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.curation.ListCurationJob listCurationJob)
    {
      if(listCurationJob == null) return this.EQ((java.lang.String)null);
      return this.EQ(listCurationJob.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.curation.ListCurationJob listCurationJob)
    {
      if(listCurationJob == null) return this.NE((java.lang.String)null);
      return this.NE(listCurationJob.getOid());
    }

  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ListCurationJobQueryMultiReferenceIF extends com.runwaysdk.system.scheduler.ExecutableJobQuery.ExecutableJobQueryMultiReferenceIF
  {


    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.curation.ListCurationJob ... listCurationJob);
    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.curation.ListCurationJob ... listCurationJob);
    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.curation.ListCurationJob ... listCurationJob);
    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.curation.ListCurationJob ... listCurationJob);
    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.curation.ListCurationJob ... listCurationJob);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class ListCurationJobQueryMultiReference extends com.runwaysdk.system.scheduler.ExecutableJobQuery.ExecutableJobQueryMultiReference
 implements ListCurationJobQueryMultiReferenceIF

  {

  public ListCurationJobQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.curation.ListCurationJob ... listCurationJob)  {

      String[] itemIdArray = new String[listCurationJob.length]; 

      for (int i=0; i<listCurationJob.length; i++)
      {
        itemIdArray[i] = listCurationJob[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.curation.ListCurationJob ... listCurationJob)  {

      String[] itemIdArray = new String[listCurationJob.length]; 

      for (int i=0; i<listCurationJob.length; i++)
      {
        itemIdArray[i] = listCurationJob[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.curation.ListCurationJob ... listCurationJob)  {

      String[] itemIdArray = new String[listCurationJob.length]; 

      for (int i=0; i<listCurationJob.length; i++)
      {
        itemIdArray[i] = listCurationJob[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.curation.ListCurationJob ... listCurationJob)  {

      String[] itemIdArray = new String[listCurationJob.length]; 

      for (int i=0; i<listCurationJob.length; i++)
      {
        itemIdArray[i] = listCurationJob[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.curation.ListCurationJob ... listCurationJob)  {

      String[] itemIdArray = new String[listCurationJob.length]; 

      for (int i=0; i<listCurationJob.length; i++)
      {
        itemIdArray[i] = listCurationJob[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  }
}
