package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -1493277184)
public abstract class ListTypeGeoObjectTypeGroupDTOBase extends net.geoprism.registry.ListTypeGroupDTO
{
  public final static String CLASS = "net.geoprism.registry.ListTypeGeoObjectTypeGroup";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1493277184;
  
  protected ListTypeGeoObjectTypeGroupDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListTypeGeoObjectTypeGroupDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String GEOOBJECTTYPE = "geoObjectType";
  public static java.lang.String LEVEL = "level";
  public Integer getLevel()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LEVEL));
  }
  
  public void setLevel(Integer value)
  {
    if(value == null)
    {
      setValue(LEVEL, "");
    }
    else
    {
      setValue(LEVEL, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLevelWritable()
  {
    return isWritable(LEVEL);
  }
  
  public boolean isLevelReadable()
  {
    return isReadable(LEVEL);
  }
  
  public boolean isLevelModified()
  {
    return isModified(LEVEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLevelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LEVEL).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO) dto;
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
  
  public static net.geoprism.registry.ListTypeGeoObjectTypeGroupQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.ListTypeGeoObjectTypeGroupQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.ListTypeGeoObjectTypeGroupDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
