package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 639819451)
public abstract class DuplicateGeoObjectMultipleExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.DuplicateGeoObjectMultipleException";
  private static final long serialVersionUID = 639819451;
  
  public DuplicateGeoObjectMultipleExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DuplicateGeoObjectMultipleExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DuplicateGeoObjectMultipleExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DuplicateGeoObjectMultipleExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DuplicateGeoObjectMultipleExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DuplicateGeoObjectMultipleExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DuplicateGeoObjectMultipleExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DuplicateGeoObjectMultipleExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ATTRIBUTELABELS = "attributeLabels";
  public static java.lang.String OID = "oid";
  public String getAttributeLabels()
  {
    return getValue(ATTRIBUTELABELS);
  }
  
  public void setAttributeLabels(String value)
  {
    if(value == null)
    {
      setValue(ATTRIBUTELABELS, "");
    }
    else
    {
      setValue(ATTRIBUTELABELS, value);
    }
  }
  
  public boolean isAttributeLabelsWritable()
  {
    return isWritable(ATTRIBUTELABELS);
  }
  
  public boolean isAttributeLabelsReadable()
  {
    return isReadable(ATTRIBUTELABELS);
  }
  
  public boolean isAttributeLabelsModified()
  {
    return isModified(ATTRIBUTELABELS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getAttributeLabelsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ATTRIBUTELABELS).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{attributeLabels}", this.getAttributeLabels().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
