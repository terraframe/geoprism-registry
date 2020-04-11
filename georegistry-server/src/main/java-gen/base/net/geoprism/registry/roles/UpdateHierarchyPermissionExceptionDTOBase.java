package net.geoprism.registry.roles;

@com.runwaysdk.business.ClassSignature(hash = 1371255094)
public abstract class UpdateHierarchyPermissionExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.roles.UpdateHierarchyPermissionException";
  private static final long serialVersionUID = 1371255094;
  
  public UpdateHierarchyPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected UpdateHierarchyPermissionExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public UpdateHierarchyPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public UpdateHierarchyPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public UpdateHierarchyPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public UpdateHierarchyPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public UpdateHierarchyPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public UpdateHierarchyPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static java.lang.String ORGANIZATION = "organization";
  public String getOrganization()
  {
    return getValue(ORGANIZATION);
  }
  
  public void setOrganization(String value)
  {
    if(value == null)
    {
      setValue(ORGANIZATION, "");
    }
    else
    {
      setValue(ORGANIZATION, value);
    }
  }
  
  public boolean isOrganizationWritable()
  {
    return isWritable(ORGANIZATION);
  }
  
  public boolean isOrganizationReadable()
  {
    return isReadable(ORGANIZATION);
  }
  
  public boolean isOrganizationModified()
  {
    return isModified(ORGANIZATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getOrganizationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ORGANIZATION).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{organization}", this.getOrganization().toString());
    
    return template;
  }
  
}
