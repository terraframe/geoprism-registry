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
package net.geoprism.registry.etl.export;

@com.runwaysdk.business.ClassSignature(hash = -1572365579)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ExportStageMaster.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ExportStageMasterBase extends com.runwaysdk.system.EnumerationMaster
{
  public final static String CLASS = "net.geoprism.registry.etl.export.ExportStageMaster";
  private static final long serialVersionUID = -1572365579;
  
  public ExportStageMasterBase()
  {
    super();
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static ExportStageMasterQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    ExportStageMasterQuery query = new ExportStageMasterQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static ExportStageMaster get(String oid)
  {
    return (ExportStageMaster) com.runwaysdk.business.Business.get(oid);
  }
  
  public static ExportStageMaster getByKey(String key)
  {
    return (ExportStageMaster) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static ExportStageMaster getEnumeration(String enumName)
  {
    return (ExportStageMaster) com.runwaysdk.business.Business.getEnumeration(net.geoprism.registry.etl.export.ExportStageMaster.CLASS ,enumName);
  }
  
  public static ExportStageMaster lock(java.lang.String oid)
  {
    ExportStageMaster _instance = ExportStageMaster.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static ExportStageMaster unlock(java.lang.String oid)
  {
    ExportStageMaster _instance = ExportStageMaster.get(oid);
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
