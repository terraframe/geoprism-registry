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
package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = 1368599953)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ImportHistory.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ImportHistoryBase extends com.runwaysdk.system.scheduler.JobHistory
{
  public final static String CLASS = "net.geoprism.registry.etl.ImportHistory";
  public final static java.lang.String COMPLETEDROWSJSON = "completedRowsJson";
  public final static java.lang.String CONFIGJSON = "configJson";
  public final static java.lang.String ERRORCOUNT = "errorCount";
  public final static java.lang.String ERRORRESOLVEDCOUNT = "errorResolvedCount";
  public final static java.lang.String GEOOBJECTTYPECODE = "geoObjectTypeCode";
  public final static java.lang.String IMPORTFILE = "importFile";
  public final static java.lang.String IMPORTEDRECORDS = "importedRecords";
  public final static java.lang.String ORGANIZATION = "organization";
  public final static java.lang.String STAGE = "stage";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1368599953;
  
  public ImportHistoryBase()
  {
    super();
  }
  
  public String getCompletedRowsJson()
  {
    return getValue(COMPLETEDROWSJSON);
  }
  
  public void validateCompletedRowsJson()
  {
    this.validateAttribute(COMPLETEDROWSJSON);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getCompletedRowsJsonMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(COMPLETEDROWSJSON);
  }
  
  public void setCompletedRowsJson(String value)
  {
    if(value == null)
    {
      setValue(COMPLETEDROWSJSON, "");
    }
    else
    {
      setValue(COMPLETEDROWSJSON, value);
    }
  }
  
  public String getConfigJson()
  {
    return getValue(CONFIGJSON);
  }
  
  public void validateConfigJson()
  {
    this.validateAttribute(CONFIGJSON);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getConfigJsonMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(CONFIGJSON);
  }
  
  public void setConfigJson(String value)
  {
    if(value == null)
    {
      setValue(CONFIGJSON, "");
    }
    else
    {
      setValue(CONFIGJSON, value);
    }
  }
  
  public Long getErrorCount()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(ERRORCOUNT));
  }
  
  public void validateErrorCount()
  {
    this.validateAttribute(ERRORCOUNT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getErrorCountMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(ERRORCOUNT);
  }
  
  public void setErrorCount(Long value)
  {
    if(value == null)
    {
      setValue(ERRORCOUNT, "");
    }
    else
    {
      setValue(ERRORCOUNT, java.lang.Long.toString(value));
    }
  }
  
  public Long getErrorResolvedCount()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(ERRORRESOLVEDCOUNT));
  }
  
  public void validateErrorResolvedCount()
  {
    this.validateAttribute(ERRORRESOLVEDCOUNT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getErrorResolvedCountMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(ERRORRESOLVEDCOUNT);
  }
  
  public void setErrorResolvedCount(Long value)
  {
    if(value == null)
    {
      setValue(ERRORRESOLVEDCOUNT, "");
    }
    else
    {
      setValue(ERRORRESOLVEDCOUNT, java.lang.Long.toString(value));
    }
  }
  
  public String getGeoObjectTypeCode()
  {
    return getValue(GEOOBJECTTYPECODE);
  }
  
  public void validateGeoObjectTypeCode()
  {
    this.validateAttribute(GEOOBJECTTYPECODE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getGeoObjectTypeCodeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(GEOOBJECTTYPECODE);
  }
  
  public void setGeoObjectTypeCode(String value)
  {
    if(value == null)
    {
      setValue(GEOOBJECTTYPECODE, "");
    }
    else
    {
      setValue(GEOOBJECTTYPECODE, value);
    }
  }
  
  public com.runwaysdk.system.VaultFile getImportFile()
  {
    if (getValue(IMPORTFILE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.VaultFile.get(getValue(IMPORTFILE));
    }
  }
  
  public String getImportFileOid()
  {
    return getValue(IMPORTFILE);
  }
  
  public void validateImportFile()
  {
    this.validateAttribute(IMPORTFILE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getImportFileMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(IMPORTFILE);
  }
  
  public void setImportFile(com.runwaysdk.system.VaultFile value)
  {
    if(value == null)
    {
      setValue(IMPORTFILE, "");
    }
    else
    {
      setValue(IMPORTFILE, value.getOid());
    }
  }
  
  public void setImportFileId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(IMPORTFILE, "");
    }
    else
    {
      setValue(IMPORTFILE, oid);
    }
  }
  
  public Long getImportedRecords()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(IMPORTEDRECORDS));
  }
  
  public void validateImportedRecords()
  {
    this.validateAttribute(IMPORTEDRECORDS);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getImportedRecordsMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(IMPORTEDRECORDS);
  }
  
  public void setImportedRecords(Long value)
  {
    if(value == null)
    {
      setValue(IMPORTEDRECORDS, "");
    }
    else
    {
      setValue(IMPORTEDRECORDS, java.lang.Long.toString(value));
    }
  }
  
  public net.geoprism.registry.Organization getOrganization()
  {
    if (getValue(ORGANIZATION).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.Organization.get(getValue(ORGANIZATION));
    }
  }
  
  public String getOrganizationOid()
  {
    return getValue(ORGANIZATION);
  }
  
  public void validateOrganization()
  {
    this.validateAttribute(ORGANIZATION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getOrganizationMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(ORGANIZATION);
  }
  
  public void setOrganization(net.geoprism.registry.Organization value)
  {
    if(value == null)
    {
      setValue(ORGANIZATION, "");
    }
    else
    {
      setValue(ORGANIZATION, value.getOid());
    }
  }
  
  public void setOrganizationId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(ORGANIZATION, "");
    }
    else
    {
      setValue(ORGANIZATION, oid);
    }
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<net.geoprism.registry.etl.ImportStage> getStage()
  {
    return (java.util.List<net.geoprism.registry.etl.ImportStage>) getEnumValues(STAGE);
  }
  
  public void addStage(net.geoprism.registry.etl.ImportStage value)
  {
    if(value != null)
    {
      addEnumItem(STAGE, value.getOid());
    }
  }
  
  public void removeStage(net.geoprism.registry.etl.ImportStage value)
  {
    if(value != null)
    {
      removeEnumItem(STAGE, value.getOid());
    }
  }
  
  public void clearStage()
  {
    clearEnum(STAGE);
  }
  
  public void validateStage()
  {
    this.validateAttribute(STAGE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeEnumerationDAOIF getStageMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.ImportHistory.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeEnumerationDAOIF)mdClassIF.definesAttribute(STAGE);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static ImportHistoryQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    ImportHistoryQuery query = new ImportHistoryQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static ImportHistory get(String oid)
  {
    return (ImportHistory) com.runwaysdk.business.Business.get(oid);
  }
  
  public static ImportHistory getByKey(String key)
  {
    return (ImportHistory) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static ImportHistory lock(java.lang.String oid)
  {
    ImportHistory _instance = ImportHistory.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static ImportHistory unlock(java.lang.String oid)
  {
    ImportHistory _instance = ImportHistory.get(oid);
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
