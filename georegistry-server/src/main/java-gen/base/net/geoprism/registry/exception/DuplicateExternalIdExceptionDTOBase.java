package net.geoprism.registry.exception;

@com.runwaysdk.business.ClassSignature(hash = 761878757)
public abstract class DuplicateExternalIdExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.exception.DuplicateExternalIdException";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 761878757;
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DuplicateExternalIdExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String EXTERNALID = "externalId";
  public static java.lang.String EXTERNALSYSTEM = "externalSystem";
  public static java.lang.String OID = "oid";
  public String getExternalId()
  {
    return getValue(EXTERNALID);
  }
  
  public void setExternalId(String value)
  {
    if(value == null)
    {
      setValue(EXTERNALID, "");
    }
    else
    {
      setValue(EXTERNALID, value);
    }
  }
  
  public boolean isExternalIdWritable()
  {
    return isWritable(EXTERNALID);
  }
  
  public boolean isExternalIdReadable()
  {
    return isReadable(EXTERNALID);
  }
  
  public boolean isExternalIdModified()
  {
    return isModified(EXTERNALID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getExternalIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(EXTERNALID).getAttributeMdDTO();
  }
  
  public String getExternalSystem()
  {
    return getValue(EXTERNALSYSTEM);
  }
  
  public void setExternalSystem(String value)
  {
    if(value == null)
    {
      setValue(EXTERNALSYSTEM, "");
    }
    else
    {
      setValue(EXTERNALSYSTEM, value);
    }
  }
  
  public boolean isExternalSystemWritable()
  {
    return isWritable(EXTERNALSYSTEM);
  }
  
  public boolean isExternalSystemReadable()
  {
    return isReadable(EXTERNALSYSTEM);
  }
  
  public boolean isExternalSystemModified()
  {
    return isModified(EXTERNALSYSTEM);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getExternalSystemMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(EXTERNALSYSTEM).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{externalId}", this.getExternalId().toString());
    template = template.replace("{externalSystem}", this.getExternalSystem().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
