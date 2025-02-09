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

@com.runwaysdk.business.ClassSignature(hash = 749315200)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ListTypeGeoObjectTypeGroup.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ListTypeGeoObjectTypeGroupBase extends net.geoprism.registry.ListTypeGroup
{
  public final static String CLASS = "net.geoprism.registry.ListTypeGeoObjectTypeGroup";
  public final static java.lang.String GEOOBJECTTYPE = "geoObjectType";
  public final static java.lang.String LEVEL = "level";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 749315200;
  
  public ListTypeGeoObjectTypeGroupBase()
  {
    super();
  }
  
  public net.geoprism.registry.graph.GeoObjectType getGeoObjectType()
  {
    return (net.geoprism.registry.graph.GeoObjectType)com.runwaysdk.business.graph.VertexObject.get("net.geoprism.registry.graph.GeoObjectType", getValue(GEOOBJECTTYPE));
  }
  
  public void validateGeoObjectType()
  {
    this.validateAttribute(GEOOBJECTTYPE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF getGeoObjectTypeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.ListTypeGeoObjectTypeGroup.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF)mdClassIF.definesAttribute(GEOOBJECTTYPE);
  }
  
  public void setGeoObjectType(net.geoprism.registry.graph.GeoObjectType value)
  {
    if(value == null)
    {
      setValue(GEOOBJECTTYPE, "");
    }
    else
    {
      setValue(GEOOBJECTTYPE, value.getOid());
    }
  }
  
  public Integer getLevel()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LEVEL));
  }
  
  public void validateLevel()
  {
    this.validateAttribute(LEVEL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF getLevelMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.ListTypeGeoObjectTypeGroup.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF)mdClassIF.definesAttribute(LEVEL);
  }
  
  public void setLevel(Integer value)
  {
    if(value == null)
    {
      setValue(LEVEL, "");
    }
    else
    {
      setValue(LEVEL, java.lang.Integer.toString(value));
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static ListTypeGeoObjectTypeGroupQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    ListTypeGeoObjectTypeGroupQuery query = new ListTypeGeoObjectTypeGroupQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static ListTypeGeoObjectTypeGroup get(String oid)
  {
    return (ListTypeGeoObjectTypeGroup) com.runwaysdk.business.Business.get(oid);
  }
  
  public static ListTypeGeoObjectTypeGroup getByKey(String key)
  {
    return (ListTypeGeoObjectTypeGroup) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static ListTypeGeoObjectTypeGroup lock(java.lang.String oid)
  {
    ListTypeGeoObjectTypeGroup _instance = ListTypeGeoObjectTypeGroup.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static ListTypeGeoObjectTypeGroup unlock(java.lang.String oid)
  {
    ListTypeGeoObjectTypeGroup _instance = ListTypeGeoObjectTypeGroup.get(oid);
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
