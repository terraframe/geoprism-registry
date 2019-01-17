package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 1716984523)
public abstract class GeometryTypeExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.GeometryTypeException";
  private static final long serialVersionUID = 1716984523;
  
  public GeometryTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected GeometryTypeExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public GeometryTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public GeometryTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public GeometryTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public GeometryTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public GeometryTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public GeometryTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ACTUALTYPE = "actualType";
  public static java.lang.String EXPECTEDTYPE = "expectedType";
  public static java.lang.String OID = "oid";
  public String getActualType()
  {
    return getValue(ACTUALTYPE);
  }
  
  public void setActualType(String value)
  {
    if(value == null)
    {
      setValue(ACTUALTYPE, "");
    }
    else
    {
      setValue(ACTUALTYPE, value);
    }
  }
  
  public boolean isActualTypeWritable()
  {
    return isWritable(ACTUALTYPE);
  }
  
  public boolean isActualTypeReadable()
  {
    return isReadable(ACTUALTYPE);
  }
  
  public boolean isActualTypeModified()
  {
    return isModified(ACTUALTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getActualTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ACTUALTYPE).getAttributeMdDTO();
  }
  
  public String getExpectedType()
  {
    return getValue(EXPECTEDTYPE);
  }
  
  public void setExpectedType(String value)
  {
    if(value == null)
    {
      setValue(EXPECTEDTYPE, "");
    }
    else
    {
      setValue(EXPECTEDTYPE, value);
    }
  }
  
  public boolean isExpectedTypeWritable()
  {
    return isWritable(EXPECTEDTYPE);
  }
  
  public boolean isExpectedTypeReadable()
  {
    return isReadable(EXPECTEDTYPE);
  }
  
  public boolean isExpectedTypeModified()
  {
    return isModified(EXPECTEDTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getExpectedTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(EXPECTEDTYPE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{actualType}", this.getActualType().toString());
    template = template.replace("{expectedType}", this.getExpectedType().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
