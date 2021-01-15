package net.geoprism.registry.account;

@com.runwaysdk.business.ClassSignature(hash = -1837268085)
public abstract class RegistryAccountUtilDTOBase extends com.runwaysdk.business.UtilDTO
{
  public final static String CLASS = "net.geoprism.registry.account.RegistryAccountUtil";
  private static final long serialVersionUID = -1837268085;
  
  protected RegistryAccountUtilDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static final void initiate(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String invite, java.lang.String serverUrl, java.lang.String roleIds)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{invite, serverUrl, roleIds};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.account.RegistryAccountUtilDTO.CLASS, "initiate", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void inviteComplete(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String token, java.lang.String user)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{token, user};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.account.RegistryAccountUtilDTO.CLASS, "inviteComplete", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.GeoprismUserDTO newUserInst(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.account.RegistryAccountUtilDTO.CLASS, "newUserInst", _declaredTypes);
    return (net.geoprism.GeoprismUserDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static RegistryAccountUtilDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.UtilDTO dto = (com.runwaysdk.business.UtilDTO)clientRequest.get(oid);
    
    return (RegistryAccountUtilDTO) dto;
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
