package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = -1487320701)
public abstract class PublishMasterListJobDTOBase extends com.runwaysdk.system.scheduler.ExecutableJobDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.PublishMasterListJob";
  private static final long serialVersionUID = -1487320701;
  
  protected PublishMasterListJobDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected PublishMasterListJobDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String MASTERLIST = "masterList";
  public net.geoprism.registry.MasterListDTO getMasterList()
  {
    if(getValue(MASTERLIST) == null || getValue(MASTERLIST).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.MasterListDTO.get(getRequest(), getValue(MASTERLIST));
    }
  }
  
  public String getMasterListOid()
  {
    return getValue(MASTERLIST);
  }
  
  public void setMasterList(net.geoprism.registry.MasterListDTO value)
  {
    if(value == null)
    {
      setValue(MASTERLIST, "");
    }
    else
    {
      setValue(MASTERLIST, value.getOid());
    }
  }
  
  public boolean isMasterListWritable()
  {
    return isWritable(MASTERLIST);
  }
  
  public boolean isMasterListReadable()
  {
    return isReadable(MASTERLIST);
  }
  
  public boolean isMasterListModified()
  {
    return isModified(MASTERLIST);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getMasterListMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(MASTERLIST).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.etl.PublishMasterListJobDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.etl.PublishMasterListJobDTO) dto;
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
  
  public static net.geoprism.registry.etl.PublishMasterListJobQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.etl.PublishMasterListJobQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.etl.PublishMasterListJobDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.etl.PublishMasterListJobDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.PublishMasterListJobDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.etl.PublishMasterListJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.etl.PublishMasterListJobDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.PublishMasterListJobDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.etl.PublishMasterListJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
