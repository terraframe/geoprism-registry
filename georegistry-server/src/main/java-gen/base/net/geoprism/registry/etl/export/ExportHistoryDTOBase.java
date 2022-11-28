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

@com.runwaysdk.business.ClassSignature(hash = -1466030967)
public abstract class ExportHistoryDTOBase extends com.runwaysdk.system.scheduler.JobHistoryDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.export.ExportHistory";
  private static final long serialVersionUID = -1466030967;
  
  protected ExportHistoryDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ExportHistoryDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static final java.lang.String EXPORTEDRECORDS = "exportedRecords";
  public static final java.lang.String STAGE = "stage";
  public Long getExportedRecords()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(EXPORTEDRECORDS));
  }
  
  public void setExportedRecords(Long value)
  {
    if(value == null)
    {
      setValue(EXPORTEDRECORDS, "");
    }
    else
    {
      setValue(EXPORTEDRECORDS, java.lang.Long.toString(value));
    }
  }
  
  public boolean isExportedRecordsWritable()
  {
    return isWritable(EXPORTEDRECORDS);
  }
  
  public boolean isExportedRecordsReadable()
  {
    return isReadable(EXPORTEDRECORDS);
  }
  
  public boolean isExportedRecordsModified()
  {
    return isModified(EXPORTEDRECORDS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getExportedRecordsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(EXPORTEDRECORDS).getAttributeMdDTO();
  }
  
  
  public java.util.List<net.geoprism.registry.etl.export.ExportStageDTO> getStage()
  {
    return (java.util.List<net.geoprism.registry.etl.export.ExportStageDTO>) com.runwaysdk.transport.conversion.ConversionFacade.convertEnumDTOsFromEnumNames(getRequest(), net.geoprism.registry.etl.export.ExportStageDTO.CLASS, getEnumNames(STAGE));
  }
  
  public java.util.List<String> getStageEnumNames()
  {
    return getEnumNames(STAGE);
  }
  
  public void addStage(net.geoprism.registry.etl.export.ExportStageDTO enumDTO)
  {
    addEnumItem(STAGE, enumDTO.toString());
  }
  
  public void removeStage(net.geoprism.registry.etl.export.ExportStageDTO enumDTO)
  {
    removeEnumItem(STAGE, enumDTO.toString());
  }
  
  public void clearStage()
  {
    clearEnum(STAGE);
  }
  
  public boolean isStageWritable()
  {
    return isWritable(STAGE);
  }
  
  public boolean isStageReadable()
  {
    return isReadable(STAGE);
  }
  
  public boolean isStageModified()
  {
    return isModified(STAGE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO getStageMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO) getAttributeDTO(STAGE).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.etl.export.ExportHistoryDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.etl.export.ExportHistoryDTO) dto;
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
  
  public static net.geoprism.registry.etl.export.ExportHistoryQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.etl.export.ExportHistoryQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.etl.export.ExportHistoryDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.etl.export.ExportHistoryDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.export.ExportHistoryDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.etl.export.ExportHistoryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.etl.export.ExportHistoryDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.export.ExportHistoryDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.etl.export.ExportHistoryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
