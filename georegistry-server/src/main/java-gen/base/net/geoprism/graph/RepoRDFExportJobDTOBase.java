package net.geoprism.graph;

@com.runwaysdk.business.ClassSignature(hash = -2049476885)
public abstract class RepoRDFExportJobDTOBase extends com.runwaysdk.system.scheduler.ExecutableJobDTO
{
  public final static String CLASS = "net.geoprism.graph.RepoRDFExportJob";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -2049476885;
  
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
  
  public static java.lang.String BUSINESSEDGECODES = "businessEdgeCodes";
  public static java.lang.String BUSINESSTYPECODES = "businessTypeCodes";
  public static java.lang.String GEOMETRYEXPORTTYPE = "geometryExportType";
  public static java.lang.String GOTCODES = "gotCodes";
  public static java.lang.String GRAPHTYPECODES = "graphTypeCodes";
  public static java.lang.String VALIDFOR = "validFor";
  public String getBusinessEdgeCodes()
  {
    return getValue(BUSINESSEDGECODES);
  }
  
  public void setBusinessEdgeCodes(String value)
  {
    if(value == null)
    {
      setValue(BUSINESSEDGECODES, "");
    }
    else
    {
      setValue(BUSINESSEDGECODES, value);
    }
  }
  
  public boolean isBusinessEdgeCodesWritable()
  {
    return isWritable(BUSINESSEDGECODES);
  }
  
  public boolean isBusinessEdgeCodesReadable()
  {
    return isReadable(BUSINESSEDGECODES);
  }
  
  public boolean isBusinessEdgeCodesModified()
  {
    return isModified(BUSINESSEDGECODES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getBusinessEdgeCodesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(BUSINESSEDGECODES).getAttributeMdDTO();
  }
  
  public String getBusinessTypeCodes()
  {
    return getValue(BUSINESSTYPECODES);
  }
  
  public void setBusinessTypeCodes(String value)
  {
    if(value == null)
    {
      setValue(BUSINESSTYPECODES, "");
    }
    else
    {
      setValue(BUSINESSTYPECODES, value);
    }
  }
  
  public boolean isBusinessTypeCodesWritable()
  {
    return isWritable(BUSINESSTYPECODES);
  }
  
  public boolean isBusinessTypeCodesReadable()
  {
    return isReadable(BUSINESSTYPECODES);
  }
  
  public boolean isBusinessTypeCodesModified()
  {
    return isModified(BUSINESSTYPECODES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getBusinessTypeCodesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(BUSINESSTYPECODES).getAttributeMdDTO();
  }
  
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
  
  public java.util.Date getValidFor()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(VALIDFOR));
  }
  
  public void setValidFor(java.util.Date value)
  {
    if(value == null)
    {
      setValue(VALIDFOR, "");
    }
    else
    {
      setValue(VALIDFOR, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATE_FORMAT).format(value));
    }
  }
  
  public boolean isValidForWritable()
  {
    return isWritable(VALIDFOR);
  }
  
  public boolean isValidForReadable()
  {
    return isReadable(VALIDFOR);
  }
  
  public boolean isValidForModified()
  {
    return isModified(VALIDFOR);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateMdDTO getValidForMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateMdDTO) getAttributeDTO(VALIDFOR).getAttributeMdDTO();
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
