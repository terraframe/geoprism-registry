package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = 686217731)
public abstract class ImportHistoryDTOBase extends com.runwaysdk.system.scheduler.JobHistoryDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.ImportHistory";
  private static final long serialVersionUID = 686217731;
  
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
  
  public static java.lang.String CONFIGJSON = "configJson";
  public static java.lang.String IMPORTFILE = "importFile";
  public static java.lang.String IMPORTEDRECORDS = "importedRecords";
  public static java.lang.String STAGE = "stage";
  public String getConfigJson()
  {
    return getValue(CONFIGJSON);
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
  
  public boolean isConfigJsonWritable()
  {
    return isWritable(CONFIGJSON);
  }
  
  public boolean isConfigJsonReadable()
  {
    return isReadable(CONFIGJSON);
  }
  
  public boolean isConfigJsonModified()
  {
    return isModified(CONFIGJSON);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getConfigJsonMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CONFIGJSON).getAttributeMdDTO();
  }
  
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
  
  public Integer getImportedRecords()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(IMPORTEDRECORDS));
  }
  
  public void setImportedRecords(Integer value)
  {
    if(value == null)
    {
      setValue(IMPORTEDRECORDS, "");
    }
    else
    {
      setValue(IMPORTEDRECORDS, java.lang.Integer.toString(value));
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
  
  @SuppressWarnings("unchecked")
  public java.util.List<net.geoprism.registry.etl.ImportStageDTO> getStage()
  {
    return (java.util.List<net.geoprism.registry.etl.ImportStageDTO>) com.runwaysdk.transport.conversion.ConversionFacade.convertEnumDTOsFromEnumNames(getRequest(), net.geoprism.registry.etl.ImportStageDTO.CLASS, getEnumNames(STAGE));
  }
  
  public java.util.List<String> getStageEnumNames()
  {
    return getEnumNames(STAGE);
  }
  
  public void addStage(net.geoprism.registry.etl.ImportStageDTO enumDTO)
  {
    addEnumItem(STAGE, enumDTO.toString());
  }
  
  public void removeStage(net.geoprism.registry.etl.ImportStageDTO enumDTO)
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
  
  public static net.geoprism.registry.etl.ImportHistoryDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.etl.ImportHistoryDTO) dto;
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
  
  public static net.geoprism.registry.etl.ImportHistoryQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.etl.ImportHistoryQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.etl.ImportHistoryDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.etl.ImportHistoryDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.ImportHistoryDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.etl.ImportHistoryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.etl.ImportHistoryDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.ImportHistoryDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.etl.ImportHistoryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
