package net.geoprism.graph;

@com.runwaysdk.business.ClassSignature(hash = 713455274)
public abstract class RepoRDFExportJobDTOBase extends com.runwaysdk.system.scheduler.ExecutableJobDTO
{
  public final static String CLASS = "net.geoprism.graph.RepoRDFExportJob";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 713455274;
  
  protected RepoRDFExportJobDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected RepoRDFExportJobDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String GEOMETRYEXPORTTYPE = "geometryExportType";
  public static java.lang.String GOTCODES = "gotCodes";
  public static java.lang.String GRAPHTYPECODES = "graphTypeCodes";
  public String getGeometryExportType()
  {
    return getValue(GEOMETRYEXPORTTYPE);
  }
  
  public void setGeometryExportType(String value)
  {
    if(value == null)
    {
      setValue(GEOMETRYEXPORTTYPE, "");
    }
    else
    {
      setValue(GEOMETRYEXPORTTYPE, value);
    }
  }
  
  public boolean isGeometryExportTypeWritable()
  {
    return isWritable(GEOMETRYEXPORTTYPE);
  }
  
  public boolean isGeometryExportTypeReadable()
  {
    return isReadable(GEOMETRYEXPORTTYPE);
  }
  
  public boolean isGeometryExportTypeModified()
  {
    return isModified(GEOMETRYEXPORTTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeometryExportTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOMETRYEXPORTTYPE).getAttributeMdDTO();
  }
  
  public String getGotCodes()
  {
    return getValue(GOTCODES);
  }
  
  public void setGotCodes(String value)
  {
    if(value == null)
    {
      setValue(GOTCODES, "");
    }
    else
    {
      setValue(GOTCODES, value);
    }
  }
  
  public boolean isGotCodesWritable()
  {
    return isWritable(GOTCODES);
  }
  
  public boolean isGotCodesReadable()
  {
    return isReadable(GOTCODES);
  }
  
  public boolean isGotCodesModified()
  {
    return isModified(GOTCODES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGotCodesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GOTCODES).getAttributeMdDTO();
  }
  
  public String getGraphTypeCodes()
  {
    return getValue(GRAPHTYPECODES);
  }
  
  public void setGraphTypeCodes(String value)
  {
    if(value == null)
    {
      setValue(GRAPHTYPECODES, "");
    }
    else
    {
      setValue(GRAPHTYPECODES, value);
    }
  }
  
  public boolean isGraphTypeCodesWritable()
  {
    return isWritable(GRAPHTYPECODES);
  }
  
  public boolean isGraphTypeCodesReadable()
  {
    return isReadable(GRAPHTYPECODES);
  }
  
  public boolean isGraphTypeCodesModified()
  {
    return isModified(GRAPHTYPECODES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGraphTypeCodesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GRAPHTYPECODES).getAttributeMdDTO();
  }
  
  public static net.geoprism.graph.RepoRDFExportJobDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.graph.RepoRDFExportJobDTO) dto;
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
  
  public static net.geoprism.graph.RepoRDFExportJobQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.graph.RepoRDFExportJobQueryDTO) clientRequest.getAllInstances(net.geoprism.graph.RepoRDFExportJobDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.graph.RepoRDFExportJobDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.graph.RepoRDFExportJobDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.graph.RepoRDFExportJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.graph.RepoRDFExportJobDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.graph.RepoRDFExportJobDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.graph.RepoRDFExportJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
