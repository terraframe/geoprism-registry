package net.geoprism.georegistry;

@com.runwaysdk.business.ClassSignature(hash = 1455344179)
public abstract class InvalidRegistryIdExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.georegistry.InvalidRegistryIdException";
  private static final long serialVersionUID = 1455344179;
  
  public InvalidRegistryIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected InvalidRegistryIdExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public InvalidRegistryIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public InvalidRegistryIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public InvalidRegistryIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public InvalidRegistryIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public InvalidRegistryIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public InvalidRegistryIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static java.lang.String REGISTRYID = "registryId";
  public String getRegistryId()
  {
    return getValue(REGISTRYID);
  }
  
  public void setRegistryId(String value)
  {
    if(value == null)
    {
      setValue(REGISTRYID, "");
    }
    else
    {
      setValue(REGISTRYID, value);
    }
  }
  
  public boolean isRegistryIdWritable()
  {
    return isWritable(REGISTRYID);
  }
  
  public boolean isRegistryIdReadable()
  {
    return isReadable(REGISTRYID);
  }
  
  public boolean isRegistryIdModified()
  {
    return isModified(REGISTRYID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getRegistryIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(REGISTRYID).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{registryId}", this.getRegistryId().toString());
    
    return template;
  }
  
}
