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
package net.geoprism.registry.action.geoobject;

@com.runwaysdk.business.ClassSignature(hash = -770574277)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to UpdateAttributeAction.java
 *
 * @author Autogenerated by RunwaySDK
 */
public  class UpdateAttributeActionQuery extends net.geoprism.registry.action.AbstractActionQuery

{

  public UpdateAttributeActionQuery(com.runwaysdk.query.QueryFactory componentQueryFactory)
  {
    super(componentQueryFactory);
    if (this.getComponentQuery() == null)
    {
      com.runwaysdk.business.BusinessQuery businessQuery = componentQueryFactory.businessQuery(this.getClassType());

       this.setBusinessQuery(businessQuery);
    }
  }

  public UpdateAttributeActionQuery(com.runwaysdk.query.ValueQuery valueQuery)
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
    return net.geoprism.registry.action.geoobject.UpdateAttributeAction.CLASS;
  }
  public com.runwaysdk.query.SelectableChar getAttributeName()
  {
    return getAttributeName(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeName(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.ATTRIBUTENAME, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeName(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.ATTRIBUTENAME, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getJson()
  {
    return getJson(null);

  }
 
  public com.runwaysdk.query.SelectableChar getJson(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.JSON, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getJson(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.getComponentQuery().get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.JSON, alias, displayLabel);

  }
  /**  
   * Returns an iterator of Business objects that match the query criteria specified
   * on this query object. 
   * @return iterator of Business objects that match the query criteria specified
   * on this query object.
   */
  public com.runwaysdk.query.OIterator<? extends UpdateAttributeAction> getIterator()
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
    return new com.runwaysdk.business.BusinessIterator<UpdateAttributeAction>(this.getComponentQuery().getMdEntityIF(), columnInfoMap, results);
  }


/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface UpdateAttributeActionQueryReferenceIF extends net.geoprism.registry.action.AbstractActionQuery.AbstractActionQueryReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getAttributeName();
    public com.runwaysdk.query.SelectableChar getAttributeName(String alias);
    public com.runwaysdk.query.SelectableChar getAttributeName(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getJson();
    public com.runwaysdk.query.SelectableChar getJson(String alias);
    public com.runwaysdk.query.SelectableChar getJson(String alias, String displayLabel);

    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.action.geoobject.UpdateAttributeAction updateAttributeAction);

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.action.geoobject.UpdateAttributeAction updateAttributeAction);

  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class UpdateAttributeActionQueryReference extends net.geoprism.registry.action.AbstractActionQuery.AbstractActionQueryReference
 implements UpdateAttributeActionQueryReferenceIF

  {

  public UpdateAttributeActionQueryReference(com.runwaysdk.dataaccess.MdAttributeRefDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }


    public com.runwaysdk.query.BasicCondition EQ(net.geoprism.registry.action.geoobject.UpdateAttributeAction updateAttributeAction)
    {
      if(updateAttributeAction == null) return this.EQ((java.lang.String)null);
      return this.EQ(updateAttributeAction.getOid());
    }

    public com.runwaysdk.query.BasicCondition NE(net.geoprism.registry.action.geoobject.UpdateAttributeAction updateAttributeAction)
    {
      if(updateAttributeAction == null) return this.NE((java.lang.String)null);
      return this.NE(updateAttributeAction.getOid());
    }

  public com.runwaysdk.query.SelectableChar getAttributeName()
  {
    return getAttributeName(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeName(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.ATTRIBUTENAME, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeName(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.ATTRIBUTENAME, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getJson()
  {
    return getJson(null);

  }
 
  public com.runwaysdk.query.SelectableChar getJson(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.JSON, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getJson(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.JSON, alias, displayLabel);

  }
  }

/**
 * Interface that masks all type unsafe query methods and defines all type safe methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public interface UpdateAttributeActionQueryMultiReferenceIF extends net.geoprism.registry.action.AbstractActionQuery.AbstractActionQueryMultiReferenceIF
  {

    public com.runwaysdk.query.SelectableChar getAttributeName();
    public com.runwaysdk.query.SelectableChar getAttributeName(String alias);
    public com.runwaysdk.query.SelectableChar getAttributeName(String alias, String displayLabel);
    public com.runwaysdk.query.SelectableChar getJson();
    public com.runwaysdk.query.SelectableChar getJson(String alias);
    public com.runwaysdk.query.SelectableChar getJson(String alias, String displayLabel);

    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction);
    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction);
    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction);
    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction);
    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction);
  }

/**
 * Implements type safe query methods.
 * This type is used when a join is performed on this class as a reference.
 **/
  public static class UpdateAttributeActionQueryMultiReference extends net.geoprism.registry.action.AbstractActionQuery.AbstractActionQueryMultiReference
 implements UpdateAttributeActionQueryMultiReferenceIF

  {

  public UpdateAttributeActionQueryMultiReference(com.runwaysdk.dataaccess.MdAttributeMultiReferenceDAOIF mdAttributeIF, String attributeNamespace, String definingTableName, String definingTableAlias, String mdMultiReferenceTableName, com.runwaysdk.dataaccess.MdBusinessDAOIF referenceMdBusinessIF, String referenceTableAlias, com.runwaysdk.query.ComponentQuery rootQuery, java.util.Set<com.runwaysdk.query.Join> tableJoinSet, String alias, String displayLabel)
  {
    super(mdAttributeIF, attributeNamespace, definingTableName, definingTableAlias, mdMultiReferenceTableName, referenceMdBusinessIF, referenceTableAlias, rootQuery, tableJoinSet, alias, displayLabel);

  }



    public com.runwaysdk.query.Condition containsAny(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction)  {

      String[] itemIdArray = new String[updateAttributeAction.length]; 

      for (int i=0; i<updateAttributeAction.length; i++)
      {
        itemIdArray[i] = updateAttributeAction[i].getOid();
      }

      return this.containsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAny(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction)  {

      String[] itemIdArray = new String[updateAttributeAction.length]; 

      for (int i=0; i<updateAttributeAction.length; i++)
      {
        itemIdArray[i] = updateAttributeAction[i].getOid();
      }

      return this.notContainsAny(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsAll(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction)  {

      String[] itemIdArray = new String[updateAttributeAction.length]; 

      for (int i=0; i<updateAttributeAction.length; i++)
      {
        itemIdArray[i] = updateAttributeAction[i].getOid();
      }

      return this.containsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition notContainsAll(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction)  {

      String[] itemIdArray = new String[updateAttributeAction.length]; 

      for (int i=0; i<updateAttributeAction.length; i++)
      {
        itemIdArray[i] = updateAttributeAction[i].getOid();
      }

      return this.notContainsAll(itemIdArray);
  }

    public com.runwaysdk.query.Condition containsExactly(net.geoprism.registry.action.geoobject.UpdateAttributeAction ... updateAttributeAction)  {

      String[] itemIdArray = new String[updateAttributeAction.length]; 

      for (int i=0; i<updateAttributeAction.length; i++)
      {
        itemIdArray[i] = updateAttributeAction[i].getOid();
      }

      return this.containsExactly(itemIdArray);
  }
  public com.runwaysdk.query.SelectableChar getAttributeName()
  {
    return getAttributeName(null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeName(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.ATTRIBUTENAME, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getAttributeName(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.ATTRIBUTENAME, alias, displayLabel);

  }
  public com.runwaysdk.query.SelectableChar getJson()
  {
    return getJson(null);

  }
 
  public com.runwaysdk.query.SelectableChar getJson(String alias)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.JSON, alias, null);

  }
 
  public com.runwaysdk.query.SelectableChar getJson(String alias, String displayLabel)
  {
    return (com.runwaysdk.query.SelectableChar)this.get(net.geoprism.registry.action.geoobject.UpdateAttributeAction.JSON, alias, displayLabel);

  }
  }
}
