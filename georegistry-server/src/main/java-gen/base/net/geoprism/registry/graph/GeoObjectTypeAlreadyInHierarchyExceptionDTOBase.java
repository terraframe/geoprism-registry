package net.geoprism.registry.graph;

@com.runwaysdk.business.ClassSignature(hash = -464488860)
public abstract class GeoObjectTypeAlreadyInHierarchyExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.graph.GeoObjectTypeAlreadyInHierarchyException";
  private static final long serialVersionUID = -464488860;
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String GOTCODE = "gotCode";
  public static java.lang.String OID = "oid";
  public String getGotCode()
  {
    return getValue(GOTCODE);
  }
  
  public void setGotCode(String value)
  {
    if(value == null)
    {
      setValue(GOTCODE, "");
    }
    else
    {
      setValue(GOTCODE, value);
    }
  }
  
  public boolean isGotCodeWritable()
  {
    return isWritable(GOTCODE);
  }
  
  public boolean isGotCodeReadable()
  {
    return isReadable(GOTCODE);
  }
  
  public boolean isGotCodeModified()
  {
    return isModified(GOTCODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGotCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GOTCODE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{gotCode}", this.getGotCode().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
