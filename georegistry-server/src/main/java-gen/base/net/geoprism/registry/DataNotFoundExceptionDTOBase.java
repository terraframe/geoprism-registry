package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -527452329)
public abstract class DataNotFoundExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.DataNotFoundException";
  private static final long serialVersionUID = -527452329;
  
  public DataNotFoundExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DataNotFoundExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DataNotFoundExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DataNotFoundExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DataNotFoundExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DataNotFoundExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DataNotFoundExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DataNotFoundExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String DATAIDENTIFIER = "dataIdentifier";
  public static java.lang.String OID = "oid";
  public String getDataIdentifier()
  {
    return getValue(DATAIDENTIFIER);
  }
  
  public void setDataIdentifier(String value)
  {
    if(value == null)
    {
      setValue(DATAIDENTIFIER, "");
    }
    else
    {
      setValue(DATAIDENTIFIER, value);
    }
  }
  
  public boolean isDataIdentifierWritable()
  {
    return isWritable(DATAIDENTIFIER);
  }
  
  public boolean isDataIdentifierReadable()
  {
    return isReadable(DATAIDENTIFIER);
  }
  
  public boolean isDataIdentifierModified()
  {
    return isModified(DATAIDENTIFIER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getDataIdentifierMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(DATAIDENTIFIER).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{dataIdentifier}", this.getDataIdentifier().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
