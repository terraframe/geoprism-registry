package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = -1345118289)
public abstract class UnresolvableResourceExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.UnresolvableResourceException";
  private static final long serialVersionUID = -1345118289;
  
  public UnresolvableResourceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected UnresolvableResourceExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public UnresolvableResourceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public UnresolvableResourceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public UnresolvableResourceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public UnresolvableResourceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public UnresolvableResourceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public UnresolvableResourceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String IDENTIFIER = "identifier";
  public static java.lang.String OID = "oid";
  public String getIdentifier()
  {
    return getValue(IDENTIFIER);
  }
  
  public void setIdentifier(String value)
  {
    if(value == null)
    {
      setValue(IDENTIFIER, "");
    }
    else
    {
      setValue(IDENTIFIER, value);
    }
  }
  
  public boolean isIdentifierWritable()
  {
    return isWritable(IDENTIFIER);
  }
  
  public boolean isIdentifierReadable()
  {
    return isReadable(IDENTIFIER);
  }
  
  public boolean isIdentifierModified()
  {
    return isModified(IDENTIFIER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getIdentifierMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(IDENTIFIER).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{identifier}", this.getIdentifier().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
