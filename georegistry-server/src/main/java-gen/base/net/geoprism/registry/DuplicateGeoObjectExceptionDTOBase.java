package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -643306804)
public abstract class DuplicateGeoObjectExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.DuplicateGeoObjectException";
  private static final long serialVersionUID = -643306804;
  
  public DuplicateGeoObjectExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DuplicateGeoObjectExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DuplicateGeoObjectExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DuplicateGeoObjectExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DuplicateGeoObjectExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DuplicateGeoObjectExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DuplicateGeoObjectExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DuplicateGeoObjectExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ATTRIBUTENAME = "attributeName";
  public static java.lang.String CODE = "code";
  public static java.lang.String GEOOBJECTTYPE = "geoObjectType";
  public static java.lang.String OID = "oid";
  public String getAttributeName()
  {
    return getValue(ATTRIBUTENAME);
  }
  
  public void setAttributeName(String value)
  {
    if(value == null)
    {
      setValue(ATTRIBUTENAME, "");
    }
    else
    {
      setValue(ATTRIBUTENAME, value);
    }
  }
  
  public boolean isAttributeNameWritable()
  {
    return isWritable(ATTRIBUTENAME);
  }
  
  public boolean isAttributeNameReadable()
  {
    return isReadable(ATTRIBUTENAME);
  }
  
  public boolean isAttributeNameModified()
  {
    return isModified(ATTRIBUTENAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getAttributeNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ATTRIBUTENAME).getAttributeMdDTO();
  }
  
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
  
  public String getGeoObjectType()
  {
    return getValue(GEOOBJECTTYPE);
  }
  
  public void setGeoObjectType(String value)
  {
    if(value == null)
    {
      setValue(GEOOBJECTTYPE, "");
    }
    else
    {
      setValue(GEOOBJECTTYPE, value);
    }
  }
  
  public boolean isGeoObjectTypeWritable()
  {
    return isWritable(GEOOBJECTTYPE);
  }
  
  public boolean isGeoObjectTypeReadable()
  {
    return isReadable(GEOOBJECTTYPE);
  }
  
  public boolean isGeoObjectTypeModified()
  {
    return isModified(GEOOBJECTTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeoObjectTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOOBJECTTYPE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{attributeName}", this.getAttributeName().toString());
    template = template.replace("{code}", this.getCode().toString());
    template = template.replace("{geoObjectType}", this.getGeoObjectType().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
