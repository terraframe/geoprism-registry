package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = -1834418777)
public abstract class FhirResponseExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.FhirResponseException";
  private static final long serialVersionUID = -1834418777;
  
  public FhirResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected FhirResponseExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public FhirResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public FhirResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public FhirResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public FhirResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public FhirResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public FhirResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ERRORMESSAGE = "errorMessage";
  public static java.lang.String OID = "oid";
  public String getErrorMessage()
  {
    return getValue(ERRORMESSAGE);
  }
  
  public void setErrorMessage(String value)
  {
    if(value == null)
    {
      setValue(ERRORMESSAGE, "");
    }
    else
    {
      setValue(ERRORMESSAGE, value);
    }
  }
  
  public boolean isErrorMessageWritable()
  {
    return isWritable(ERRORMESSAGE);
  }
  
  public boolean isErrorMessageReadable()
  {
    return isReadable(ERRORMESSAGE);
  }
  
  public boolean isErrorMessageModified()
  {
    return isModified(ERRORMESSAGE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getErrorMessageMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ERRORMESSAGE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{errorMessage}", this.getErrorMessage().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
