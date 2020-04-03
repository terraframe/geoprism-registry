package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -2020835456)
public abstract class OrganizationRCExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.OrganizationRCException";
  private static final long serialVersionUID = -2020835456;
  
  public OrganizationRCExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected OrganizationRCExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public OrganizationRCExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public OrganizationRCExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public OrganizationRCExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public OrganizationRCExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public OrganizationRCExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public OrganizationRCExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String GEOOBJECTTYPELABEL = "geoObjectTypeLabel";
  public static java.lang.String OID = "oid";
  public static java.lang.String ORGANIZATIONLABEL = "organizationLabel";
  public String getGeoObjectTypeLabel()
  {
    return getValue(GEOOBJECTTYPELABEL);
  }
  
  public void setGeoObjectTypeLabel(String value)
  {
    if(value == null)
    {
      setValue(GEOOBJECTTYPELABEL, "");
    }
    else
    {
      setValue(GEOOBJECTTYPELABEL, value);
    }
  }
  
  public boolean isGeoObjectTypeLabelWritable()
  {
    return isWritable(GEOOBJECTTYPELABEL);
  }
  
  public boolean isGeoObjectTypeLabelReadable()
  {
    return isReadable(GEOOBJECTTYPELABEL);
  }
  
  public boolean isGeoObjectTypeLabelModified()
  {
    return isModified(GEOOBJECTTYPELABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGeoObjectTypeLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GEOOBJECTTYPELABEL).getAttributeMdDTO();
  }
  
  public String getOrganizationLabel()
  {
    return getValue(ORGANIZATIONLABEL);
  }
  
  public void setOrganizationLabel(String value)
  {
    if(value == null)
    {
      setValue(ORGANIZATIONLABEL, "");
    }
    else
    {
      setValue(ORGANIZATIONLABEL, value);
    }
  }
  
  public boolean isOrganizationLabelWritable()
  {
    return isWritable(ORGANIZATIONLABEL);
  }
  
  public boolean isOrganizationLabelReadable()
  {
    return isReadable(ORGANIZATIONLABEL);
  }
  
  public boolean isOrganizationLabelModified()
  {
    return isModified(ORGANIZATIONLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getOrganizationLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(ORGANIZATIONLABEL).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{geoObjectTypeLabel}", this.getGeoObjectTypeLabel().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{organizationLabel}", this.getOrganizationLabel().toString());
    
    return template;
  }
  
}
