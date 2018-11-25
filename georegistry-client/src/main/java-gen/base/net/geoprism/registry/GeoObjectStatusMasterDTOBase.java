package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 1337005929)
public abstract class GeoObjectStatusMasterDTOBase extends com.runwaysdk.system.EnumerationMasterDTO
{
  public final static String CLASS = "net.geoprism.registry.GeoObjectStatusMaster";
  private static final long serialVersionUID = 1337005929;
  
  protected GeoObjectStatusMasterDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected GeoObjectStatusMasterDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String STATUSORDER = "statusOrder";
  public Integer getStatusOrder()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(STATUSORDER));
  }
  
  public void setStatusOrder(Integer value)
  {
    if(value == null)
    {
      setValue(STATUSORDER, "");
    }
    else
    {
      setValue(STATUSORDER, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isStatusOrderWritable()
  {
    return isWritable(STATUSORDER);
  }
  
  public boolean isStatusOrderReadable()
  {
    return isReadable(STATUSORDER);
  }
  
  public boolean isStatusOrderModified()
  {
    return isModified(STATUSORDER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getStatusOrderMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(STATUSORDER).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.GeoObjectStatusMasterDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.GeoObjectStatusMasterDTO) dto;
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
  
  public static net.geoprism.registry.GeoObjectStatusMasterQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.GeoObjectStatusMasterQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.GeoObjectStatusMasterDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.GeoObjectStatusMasterDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoObjectStatusMasterDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.GeoObjectStatusMasterDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.GeoObjectStatusMasterDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoObjectStatusMasterDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.GeoObjectStatusMasterDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
