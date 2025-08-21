package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = -1387236160)
public abstract class JSONFormatExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.JSONFormatException";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1387236160;
  
  public JSONFormatExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected JSONFormatExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public JSONFormatExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public JSONFormatExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public JSONFormatExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public JSONFormatExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public JSONFormatExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public JSONFormatExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static java.lang.String ROOTCAUSE = "rootCause";
  public String getRootCause()
  {
    return getValue(ROOTCAUSE);
  }
  
  public void setRootCause(String value)
  {
    if(value == null)
    {
      setValue(ROOTCAUSE, "");
    }
    else
    {
      setValue(ROOTCAUSE, value);
    }
  }
  
  public boolean isRootCauseWritable()
  {
    return isWritable(ROOTCAUSE);
  }
  
  public boolean isRootCauseReadable()
  {
    return isReadable(ROOTCAUSE);
  }
  
  public boolean isRootCauseModified()
  {
    return isModified(ROOTCAUSE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getRootCauseMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ROOTCAUSE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{rootCause}", this.getRootCause().toString());
    
    return template;
  }
  
}
