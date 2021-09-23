package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = 1992053210)
public abstract class FhirExportJobDTOBase extends net.geoprism.registry.etl.MasterListJobDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.FhirExportJob";
  private static final long serialVersionUID = 1992053210;
  
  protected FhirExportJobDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected FhirExportJobDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String EXTERNALSYSTEM = "externalSystem";
  public static java.lang.String IMPLEMENTATION = "implementation";
  public static java.lang.String VERSION = "version";
  public String getImplementation()
  {
    return getValue(IMPLEMENTATION);
  }
  
  public void setImplementation(String value)
  {
    if(value == null)
    {
      setValue(IMPLEMENTATION, "");
    }
    else
    {
      setValue(IMPLEMENTATION, value);
    }
  }
  
  public boolean isImplementationWritable()
  {
    return isWritable(IMPLEMENTATION);
  }
  
  public boolean isImplementationReadable()
  {
    return isReadable(IMPLEMENTATION);
  }
  
  public boolean isImplementationModified()
  {
    return isModified(IMPLEMENTATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getImplementationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(IMPLEMENTATION).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.MasterListVersionDTO getVersion()
  {
    if(getValue(VERSION) == null || getValue(VERSION).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.MasterListVersionDTO.get(getRequest(), getValue(VERSION));
    }
  }
  
  public String getVersionOid()
  {
    return getValue(VERSION);
  }
  
  public void setVersion(net.geoprism.registry.MasterListVersionDTO value)
  {
    if(value == null)
    {
      setValue(VERSION, "");
    }
    else
    {
      setValue(VERSION, value.getOid());
    }
  }
  
  public boolean isVersionWritable()
  {
    return isWritable(VERSION);
  }
  
  public boolean isVersionReadable()
  {
    return isReadable(VERSION);
  }
  
  public boolean isVersionModified()
  {
    return isModified(VERSION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getVersionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(VERSION).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.etl.FhirExportJobDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.etl.FhirExportJobDTO) dto;
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
  
  public static net.geoprism.registry.etl.FhirExportJobQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.etl.FhirExportJobQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.etl.FhirExportJobDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.etl.FhirExportJobDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.FhirExportJobDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.etl.FhirExportJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.etl.FhirExportJobDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.FhirExportJobDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.etl.FhirExportJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
