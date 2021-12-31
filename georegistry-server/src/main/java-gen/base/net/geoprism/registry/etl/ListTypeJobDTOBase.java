package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = 1415752253)
public abstract class ListTypeJobDTOBase extends com.runwaysdk.system.scheduler.ExecutableJobDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.ListTypeJob";
  private static final long serialVersionUID = 1415752253;
  
  protected ListTypeJobDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListTypeJobDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String LISTTYPE = "listType";
  public boolean isListTypeWritable()
  {
    return isWritable(LISTTYPE);
  }
  
  public boolean isListTypeReadable()
  {
    return isReadable(LISTTYPE);
  }
  
  public boolean isListTypeModified()
  {
    return isModified(LISTTYPE);
  }
  
  public static net.geoprism.registry.etl.ListTypeJobDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.etl.ListTypeJobDTO) dto;
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
  
  public static net.geoprism.registry.etl.ListTypeJobQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.etl.ListTypeJobQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.etl.ListTypeJobDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.etl.ListTypeJobDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.ListTypeJobDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.etl.ListTypeJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.etl.ListTypeJobDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.ListTypeJobDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.etl.ListTypeJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
