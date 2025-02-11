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

@com.runwaysdk.business.ClassSignature(hash = 593188727)
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
  public com.runwaysdk.query.SelectableUUID getGeoObjectType()
  {
    return getGeoObjectType(null);

  }
 
  public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias)
  {
    return (com.runwaysdk.query.SelectableUUID)this.getComponentQuery().get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.GEOOBJECTTYPE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableUUID)this.getComponentQuery().get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.GEOOBJECTTYPE, alias, displayLabel);

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

    public com.runwaysdk.query.SelectableUUID getGeoObjectType();
    public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias);
    public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableInteger getLevel();
    public com.runwaysdk.query.SelectableInteger getLevel(String alias);
    public com.runwaysdk.query.SelectableInteger getLevel(String alias, String displayLabel);

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

  public com.runwaysdk.query.SelectableUUID getGeoObjectType()
  {
    return getGeoObjectType(null);

  }
 
  public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.GEOOBJECTTYPE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.GEOOBJECTTYPE, alias, displayLabel);

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
  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface ListTypeGeoObjectTypeGroupQueryMultiReferenceIF extends net.geoprism.registry.ListTypeGroupQuery.ListTypeGroupQueryMultiReferenceIF
  {

    public com.runwaysdk.query.SelectableUUID getGeoObjectType();
    public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias);
    public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableInteger getLevel();
    public com.runwaysdk.query.SelectableInteger getLevel(String alias);
    public com.runwaysdk.query.SelectableInteger getLevel(String alias, String displayLabel);

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
  public com.runwaysdk.query.SelectableUUID getGeoObjectType()
  {
    return getGeoObjectType(null);

  }
 
  public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.GEOOBJECTTYPE, alias, null);

  }
 
  public com.runwaysdk.query.SelectableUUID getGeoObjectType(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableUUID)this.get(net.geoprism.registry.ListTypeGeoObjectTypeGroup.GEOOBJECTTYPE, alias, displayLabel);

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
  }
}
