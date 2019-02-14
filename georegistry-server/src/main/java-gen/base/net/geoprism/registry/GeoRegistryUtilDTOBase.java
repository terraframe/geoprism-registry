package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -982196084)
public abstract class GeoRegistryUtilDTOBase extends com.runwaysdk.business.UtilDTO
{
  public final static String CLASS = "net.geoprism.registry.GeoRegistryUtil";
  private static final long serialVersionUID = -982196084;
  
  protected GeoRegistryUtilDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static final java.lang.String createHierarchyType(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String htJSON)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{htJSON};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoRegistryUtilDTO.CLASS, "createHierarchyType", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void submitChangeRequest(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String sJson)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{sJson};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoRegistryUtilDTO.CLASS, "submitChangeRequest", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static GeoRegistryUtilDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.UtilDTO dto = (com.runwaysdk.business.UtilDTO)clientRequest.get(oid);
    
    return (GeoRegistryUtilDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createSessionComponent(this);
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
  
}
