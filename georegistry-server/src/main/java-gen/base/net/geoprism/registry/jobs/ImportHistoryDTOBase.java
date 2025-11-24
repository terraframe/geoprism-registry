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
package net.geoprism.registry.jobs;

@com.runwaysdk.business.ClassSignature(hash = 684965105)
public abstract class ImportHistoryDTOBase extends net.geoprism.registry.jobs.GPRJobHistoryDTO
{
  public final static String CLASS = "net.geoprism.registry.jobs.ImportHistory";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 684965105;
  
  protected ImportHistoryDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ImportHistoryDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String IMPORTFILE = "importFile";
  public static java.lang.String IMPORTEDRECORDS = "importedRecords";
  public com.runwaysdk.system.VaultFileDTO getImportFile()
  {
    if(getValue(IMPORTFILE) == null || getValue(IMPORTFILE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.VaultFileDTO.get(getRequest(), getValue(IMPORTFILE));
    }
  }
  
  public String getImportFileOid()
  {
    return getValue(IMPORTFILE);
  }
  
  public void setImportFile(com.runwaysdk.system.VaultFileDTO value)
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
  
  public boolean isImportFileWritable()
  {
    return isWritable(IMPORTFILE);
  }
  
  public boolean isImportFileReadable()
  {
    return isReadable(IMPORTFILE);
  }
  
  public boolean isImportFileModified()
  {
    return isModified(IMPORTFILE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getImportFileMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(IMPORTFILE).getAttributeMdDTO();
  }
  
  public Long getImportedRecords()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(IMPORTEDRECORDS));
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
  
  public boolean isImportedRecordsWritable()
  {
    return isWritable(IMPORTEDRECORDS);
  }
  
  public boolean isImportedRecordsReadable()
  {
    return isReadable(IMPORTEDRECORDS);
  }
  
  public boolean isImportedRecordsModified()
  {
    return isModified(IMPORTEDRECORDS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getImportedRecordsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(IMPORTEDRECORDS).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.jobs.ImportHistoryDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.jobs.ImportHistoryDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createBusiness(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
  public static net.geoprism.registry.jobs.ImportHistoryQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.jobs.ImportHistoryQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.jobs.ImportHistoryDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.jobs.ImportHistoryDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.jobs.ImportHistoryDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.jobs.ImportHistoryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.jobs.ImportHistoryDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.jobs.ImportHistoryDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.jobs.ImportHistoryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
