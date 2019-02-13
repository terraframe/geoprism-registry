package net.geoprism.registry.io;

@com.runwaysdk.business.ClassSignature(hash = -1897560719)
public abstract class PostalCodeLocationExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.io.PostalCodeLocationException";
  private static final long serialVersionUID = -1897560719;
  
  public PostalCodeLocationExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected PostalCodeLocationExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public PostalCodeLocationExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public PostalCodeLocationExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public PostalCodeLocationExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public PostalCodeLocationExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public PostalCodeLocationExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public PostalCodeLocationExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CODE = "code";
  public static java.lang.String OID = "oid";
  public static java.lang.String TYPELABEL = "typeLabel";
  public String getCode()
  {
    return getValue(CODE);
  }
  
  public void setCode(String value)
  {
    if(value == null)
    {
      setValue(CODE, "");
    }
    else
    {
      setValue(CODE, value);
    }
  }
  
  public boolean isCodeWritable()
  {
    return isWritable(CODE);
  }
  
  public boolean isCodeReadable()
  {
    return isReadable(CODE);
  }
  
  public boolean isCodeModified()
  {
    return isModified(CODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CODE).getAttributeMdDTO();
  }
  
  public String getTypeLabel()
  {
    return getValue(TYPELABEL);
  }
  
  public void setTypeLabel(String value)
  {
    if(value == null)
    {
      setValue(TYPELABEL, "");
    }
    else
    {
      setValue(TYPELABEL, value);
    }
  }
  
  public boolean isTypeLabelWritable()
  {
    return isWritable(TYPELABEL);
  }
  
  public boolean isTypeLabelReadable()
  {
    return isReadable(TYPELABEL);
  }
  
  public boolean isTypeLabelModified()
  {
    return isModified(TYPELABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getTypeLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(TYPELABEL).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{code}", this.getCode().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{typeLabel}", this.getTypeLabel().toString());
    
    return template;
  }
  
}
