package net.geoprism.registry.jobs;

@com.runwaysdk.business.ClassSignature(hash = 1398298683)
public abstract class GPRJobHistoryDTOBase extends com.runwaysdk.system.scheduler.JobHistoryDTO
{
  public final static String CLASS = "net.geoprism.registry.jobs.GPRJobHistory";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1398298683;
  
  protected GPRJobHistoryDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected GPRJobHistoryDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String COMPLETEDROWSJSON = "completedRowsJson";
  public static java.lang.String CONFIGJSON = "configJson";
  public static java.lang.String GEOOBJECTTYPECODE = "geoObjectTypeCode";
  public static java.lang.String ORGANIZATION = "organization";
  public static java.lang.String STAGE = "stage";
  public String getCompletedRowsJson()
  {
    return getValue(COMPLETEDROWSJSON);
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
  
  public boolean isCompletedRowsJsonWritable()
  {
    return isWritable(COMPLETEDROWSJSON);
  }
  
  public boolean isCompletedRowsJsonReadable()
  {
    return isReadable(COMPLETEDROWSJSON);
  }
  
  public boolean isCompletedRowsJsonModified()
  {
    return isModified(COMPLETEDROWSJSON);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCompletedRowsJsonMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(COMPLETEDROWSJSON).getAttributeMdDTO();
  }
  
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
  
  public String getGeoObjectTypeCode()
  {
    return getValue(GEOOBJECTTYPECODE);
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
  
  public boolean isGeoObjectTypeCodeWritable()
  {
    return isWritable(GEOOBJECTTYPECODE);
  }
  
  public boolean isGeoObjectTypeCodeReadable()
  {
    return isReadable(GEOOBJECTTYPECODE);
  }
  
  public boolean isGeoObjectTypeCodeModified()
  {
    return isModified(GEOOBJECTTYPECODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeoObjectTypeCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOOBJECTTYPECODE).getAttributeMdDTO();
  }
  
  public boolean isOrganizationWritable()
  {
    return isWritable(ORGANIZATION);
  }
  
  public boolean isOrganizationReadable()
  {
    return isReadable(ORGANIZATION);
  }
  
  public boolean isOrganizationModified()
  {
    return isModified(ORGANIZATION);
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
  
  public static net.geoprism.registry.jobs.GPRJobHistoryDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.jobs.GPRJobHistoryDTO) dto;
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
  
  public static net.geoprism.registry.jobs.GPRJobHistoryQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.jobs.GPRJobHistoryQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.jobs.GPRJobHistoryDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.jobs.GPRJobHistoryDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.jobs.GPRJobHistoryDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.jobs.GPRJobHistoryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.jobs.GPRJobHistoryDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.jobs.GPRJobHistoryDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.jobs.GPRJobHistoryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
