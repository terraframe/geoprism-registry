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

@com.runwaysdk.business.ClassSignature(hash = -701168202)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to SetParentAction.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class SetParentActionBase extends net.geoprism.registry.action.AbstractAction
{
  public final static String CLASS = "net.geoprism.registry.action.geoobject.SetParentAction";
  public static final java.lang.String CHILDCODE = "childCode";
  public static final java.lang.String CHILDTYPECODE = "childTypeCode";
  public static final java.lang.String JSON = "json";
  private static final long serialVersionUID = -701168202;
  
  public SetParentActionBase()
  {
    super();
  }
  
  public String getChildCode()
  {
    return getValue(CHILDCODE);
  }
  
  public void validateChildCode()
  {
    this.validateAttribute(CHILDCODE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getChildCodeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.action.geoobject.SetParentAction.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(CHILDCODE);
  }
  
  public void setChildCode(String value)
  {
    if(value == null)
    {
      setValue(CHILDCODE, "");
    }
    else
    {
      setValue(CHILDCODE, value);
    }
  }
  
  public String getChildTypeCode()
  {
    return getValue(CHILDTYPECODE);
  }
  
  public void validateChildTypeCode()
  {
    this.validateAttribute(CHILDTYPECODE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getChildTypeCodeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.action.geoobject.SetParentAction.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(CHILDTYPECODE);
  }
  
  public void setChildTypeCode(String value)
  {
    if(value == null)
    {
      setValue(CHILDTYPECODE, "");
    }
    else
    {
      setValue(CHILDTYPECODE, value);
    }
  }
  
  public String getJson()
  {
    return getValue(JSON);
  }
  
  public void validateJson()
  {
    this.validateAttribute(JSON);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getJsonMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.action.geoobject.SetParentAction.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(JSON);
  }
  
  public void setJson(String value)
  {
    if(value == null)
    {
      setValue(JSON, "");
    }
    else
    {
      setValue(JSON, value);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static SetParentAction get(String oid)
  {
    return (SetParentAction) com.runwaysdk.business.Business.get(oid);
  }
  
  public static SetParentAction getByKey(String key)
  {
    return (SetParentAction) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static SetParentAction lock(java.lang.String oid)
  {
    SetParentAction _instance = SetParentAction.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static SetParentAction unlock(java.lang.String oid)
  {
    SetParentAction _instance = SetParentAction.get(oid);
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
